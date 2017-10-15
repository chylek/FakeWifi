package eu.chylek.adam.fakewifi;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.DhcpInfo;
import android.net.NetworkInfo;
import android.net.wifi.SupplicantState;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.StrictMode;
import android.util.Log;

import com.crossbowffs.remotepreferences.RemotePreferences;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.util.Collections;
import java.util.List;
import java.util.regex.Pattern;

import de.robv.android.xposed.*;
import de.robv.android.xposed.callbacks.XC_LoadPackage.LoadPackageParam;

import static eu.chylek.adam.fakewifi.Utils.PREFERENCE_NAME;

public class XposedMain implements IXposedHookLoadPackage
{
  private SharedPreferences pref;
  private LoadPackageParam lpparam;


  private boolean isDebug()
  {
      if (pref==null){
          return true;
      }
      return pref.getBoolean("debug", false);
  }
    // whether stack trace should be included in logs
    // install 'Preferences Manager' to change default (false)
    private boolean includeStack()
    {
        return pref.getBoolean("withStack", false);
    }

  public boolean hack_enabled()
  {
      boolean master_switch = pref.getBoolean("master", true);
      boolean app_enabled = pref.getBoolean(lpparam.packageName, false);
      return (master_switch && app_enabled);
  }

  public void dump_stack_trace()
  {
      Log.d("FakeWifi", Log.getStackTraceString(new Exception()));
  }

  public void log(String s)
  {
      if (!isDebug()) {
          return;
      }
      Log.d("FakeWifi", lpparam.packageName + " " + s);
  }

  public void log_call(String s)
  {
      if (!isDebug())
	  return;

      Log.d("FakeWifi", lpparam.packageName + " " + s);
      
      if (includeStack())
	     dump_stack_trace();
  }


  public Object createWifiSsid() throws Exception
  {
      // essentially does
      // WifiSsid ssid = WifiSsid.createFromAsciiEncoded("FakeWifi");

      Class cls = XposedHelpers.findClass("android.net.wifi.WifiSsid", lpparam.classLoader);
      Object wifissid = XposedHelpers.callStaticMethod(cls, "createFromAsciiEncoded", getPrefString(PrefsFragment.PREF_SSID,"FakeWifi"));
      return wifissid;
  }

  public String getPrefString(String key, String defaultvalue){
      String res = pref.getString(key,defaultvalue);
      if (res.equals("")) {
          return defaultvalue;
      }
      return res;
  }

  public WifiInfo createWifiInfo() throws Exception
  {
      // WifiInfo info = new WifiInfo();
      WifiInfo info = (WifiInfo) XposedHelpers.newInstance(WifiInfo.class);

      // NEEDED ?
      //    private boolean mHiddenSSID;

      IPInfo ip = getIPInfo();
      InetAddress addr = (ip != null ? ip.addr : null);
      XposedHelpers.setIntField(info, "mNetworkId", 1);
      XposedHelpers.setObjectField(info, "mSupplicantState", SupplicantState.COMPLETED);
      XposedHelpers.setObjectField(info, "mBSSID", getPrefString(PrefsFragment.PREF_BSSID,"66:55:44:33:22:11"));
      XposedHelpers.setObjectField(info, "mMacAddress", getPrefString(PrefsFragment.PREF_MAC,"11:22:33:44:55:66"));
      XposedHelpers.setObjectField(info, "mIpAddress", addr);
      XposedHelpers.setIntField(info, "mLinkSpeed", 65);  // Mbps
      if (Build.VERSION.SDK_INT >= 21) XposedHelpers.setIntField(info, "mFrequency", 5000); // MHz
      XposedHelpers.setIntField(info, "mRssi", 200); // MAX_RSSI

      try
      {  XposedHelpers.setObjectField(info, "mWifiSsid", createWifiSsid()); } // Kitkat
      catch (Error e)
      {  XposedHelpers.setObjectField(info, "mSSID", getPrefString(PrefsFragment.PREF_SSID,"FakeWifi"));  }	      // Jellybean

      return info;
  }

  public static class IPInfo
  {
      NetworkInterface intf;
      InetAddress addr;
      String ip;
      int ip_hex;
      int netmask_hex;
  }

  // get current ip and netmask
  public static IPInfo getIPInfo()
  {
      try
      {
	  List<NetworkInterface> interfaces = Collections.list(NetworkInterface.getNetworkInterfaces());
	  for (NetworkInterface intf : interfaces)
	  {
	      List<InetAddress> addrs = Collections.list(intf.getInetAddresses());
	      for (InetAddress addr : addrs)
	      {
		  if (!addr.isLoopbackAddress())
		  {
		      String sAddr = addr.getHostAddress().toUpperCase();
		      boolean isIPv4 = isIPv4Address(sAddr);
		      if (isIPv4)
		      {
			  IPInfo info = new IPInfo();
			  info.addr = addr;
			  info.intf = intf;
			  info.ip = sAddr;
			  info.ip_hex = InetAddress_to_hex(addr);
			  info.netmask_hex = netmask_to_hex(intf.getInterfaceAddresses().get(0).getNetworkPrefixLength());
			  return info;
		      }
		  }
	      }
	  }
      } catch (Exception ex) { } // for now eat exceptions
      return null;
  }
  
  
  public static boolean isIPv4Address(String input) {
      Pattern IPV4_PATTERN = Pattern.compile("^(25[0-5]|2[0-4]\\d|[0-1]?\\d?\\d)(\\.(25[0-5]|2[0-4]\\d|[0-1]?\\d?\\d)){3}$");
      return IPV4_PATTERN.matcher(input).matches();
  }

  public static int netmask_to_hex(int netmask_slash)
  {
      int r = 0;
      int b = 1;
      for (int i = 0; i < netmask_slash;  i++, b = b << 1)
	  r |= b;
      return r;
  }

  // for DhcpInfo
  private static int InetAddress_to_hex(InetAddress a)
  {
      int result = 0;
      byte b[] = a.getAddress();
      for (int i = 0; i < 4; i++)
	  result |= (b[i] & 0xff) << (8 * i);
      return result;
  }


  public DhcpInfo createDhcpInfo() throws Exception
  {
      DhcpInfo i = new DhcpInfo();
      IPInfo ip = getIPInfo();
      i.ipAddress = ip.ip_hex;
      i.netmask = ip.netmask_hex;
      i.dns1 = 0x04040404;
      i.dns2 = 0x08080808;
      // gateway, leaseDuration, serverAddress

      String s = ("ip address: " + String.format("%x", i.ipAddress) +
		  " netmask: /" + i.netmask +
		  "dns1: " + String.format("%x", i.dns1) +
		  "dns2: " + String.format("%x", i.dns2));
      log(s);

      return i;
  }

  // Same as XposedHelper's findAndHookMethod() but shows error msg instead of throwing exception
  // (and returns void)
  private void hook_method(Class<?> clazz, String methodName, Object... parameterTypesAndCallback)
  {
      try
      {   XposedHelpers.findAndHookMethod(clazz, methodName, parameterTypesAndCallback);  }
      catch (NoSuchMethodError e)
      {   log("couldn't hook method " + methodName);   }
  }

  // idem
  private void hook_method(String className, ClassLoader classLoader, String methodName,
                           Object... parameterTypesAndCallback)
  {
      try
      {   XposedHelpers.findAndHookMethod(className, classLoader, methodName, parameterTypesAndCallback);  }
      catch (NoSuchMethodError e)
      {   log("couldn't hook method " + methodName);   }
  }

  
  @Override
  public void handleLoadPackage(final LoadPackageParam lpp) throws Throwable
  {
      lpparam = lpp;
      // Strict mode could be upset when reading preferences, but we need them now
      StrictMode.ThreadPolicy old = StrictMode.getThreadPolicy();
      StrictMode.setThreadPolicy(new StrictMode.ThreadPolicy.Builder(old)
              .permitDiskReads()
              .build());

      if (lpparam.packageName.equals(XposedMain.class.getPackage().getName())) {
          Log.d("FakeWifi", lpparam.packageName + " return");
          return;
      }

      Context context = ContextUtils.getSystemContext();
      if (null == context) {
          Log.w("FakeWifi","null context");
          return;
      }

      if (android.os.Build.VERSION.SDK_INT>23){
          // we need content provider to access preferences because of strict permissions on Nougat.
          pref = new RemotePreferences(context, Utils.PREFERENCE_AUTHORITY, Utils.PREFERENCE_NAME);
      }
      else {
          pref = new XSharedPreferences(XposedMain.class.getPackage().getName(), PREFERENCE_NAME);
      }


      if (!hack_enabled())
      {
          return;
      }

      // --------------------------
      // following android.net.NetworkInfo hooks change every NetworkInfo sent to the app.
      // sometimes intents passed old, unaltered NetworkInfo.
      // this ensures every NetworkInfo is marked as wifi and connected.
      // (although there should always be only 1 of each type)
      hook_method("android.net.NetworkInfo", lpparam.classLoader, "getType", new XC_MethodHook() {
          @Override
          protected void afterHookedMethod(MethodHookParam param) throws Throwable {
              param.setResult(ConnectivityManager.TYPE_WIFI);
          }
      });


      hook_method("android.net.NetworkInfo", lpparam.classLoader, "isAvailable", new XC_MethodHook() {
          @Override
          protected void afterHookedMethod(MethodHookParam param) throws Throwable {
              param.setResult(true);
          }
      });


      hook_method("android.net.NetworkInfo", lpparam.classLoader, "getSubtype", new XC_MethodHook() {
          @Override
          protected void afterHookedMethod(MethodHookParam param) throws Throwable {
              param.setResult(0);
          }
      });


      hook_method("android.net.NetworkInfo", lpparam.classLoader, "getSubtypeName", new XC_MethodHook() {
          @Override
          protected void afterHookedMethod(MethodHookParam param) throws Throwable {
              param.setResult(null);
          }
      });

      hook_method("android.net.NetworkInfo", lpparam.classLoader, "getTypeName", new XC_MethodHook() {
          @Override
          protected void afterHookedMethod(MethodHookParam param) throws Throwable {
              param.setResult("WIFI");
          }
      });
      hook_method("android.net.NetworkInfo", lpparam.classLoader, "isConnectedOrConnecting", new XC_MethodHook() {
          @Override
          protected void afterHookedMethod(MethodHookParam param) throws Throwable {
              param.setResult(true);
          }
      });

      hook_method("android.net.NetworkInfo", lpparam.classLoader, "isConnected", new XC_MethodHook() {
          @Override
          protected void afterHookedMethod(MethodHookParam param) throws Throwable {
              param.setResult(true);
          }
      });

      hook_method("android.net.NetworkInfo", lpparam.classLoader, "isFailover", new XC_MethodHook() {
          @Override
          protected void afterHookedMethod(MethodHookParam param) throws Throwable {
              param.setResult(false);
          }
      });

      hook_method("android.net.NetworkInfo", lpparam.classLoader, "isRoaming", new XC_MethodHook() {
          @Override
          protected void afterHookedMethod(MethodHookParam param) throws Throwable {
              param.setResult(false);
          }
      });

      hook_method("android.net.NetworkInfo", lpparam.classLoader, "getState", new XC_MethodHook() {
          @Override
          protected void afterHookedMethod(MethodHookParam param) throws Throwable {
              param.setResult(NetworkInfo.State.CONNECTED);
          }
      });

      hook_method("android.net.NetworkInfo", lpparam.classLoader, "getDetailedState", new XC_MethodHook() {
          @Override
          protected void afterHookedMethod(MethodHookParam param) throws Throwable {
              param.setResult(NetworkInfo.DetailedState.CONNECTED);
          }
      });

      hook_method("android.net.NetworkInfo", lpparam.classLoader, "getExtraInfo", new XC_MethodHook() {
          @Override
          protected void afterHookedMethod(MethodHookParam param) throws Throwable {
              param.setResult(null);
          }
      });

      hook_method("android.net.NetworkInfo", lpparam.classLoader, "describeContents", new XC_MethodHook() {
          @Override
          protected void afterHookedMethod(MethodHookParam param) throws Throwable {
              param.setResult(0);
          }
      });
      hook_method("android.net.NetworkInfo", lpparam.classLoader, "getReason", new XC_MethodHook() {
          @Override
          protected void afterHookedMethod(MethodHookParam param) throws Throwable {
              param.setResult(null);
          }
      });

      /*
      some of the original hooks.
      Hooks for the methods that provided NetworkInfo are no longer required.
       */


      // isActiveNetworkMetered()
      hook_method("android.net.ConnectivityManager", lpparam.classLoader,
              "isActiveNetworkMetered", new XC_MethodHook()
              {
                  @Override
                  protected void afterHookedMethod(MethodHookParam param) throws Throwable
                  {
                      if (!hack_enabled())
                      {	  log_call("isActiveNetworkMetered(), hack is disabled.");  return;  }

                      log_call("isActiveNetworkMetered(), faking wifi !");
                      param.setResult(false);
                  }
              });

      // *************************************************************************************
      // WifiManager targets:
      //   isWifiEnabled()
      //   getWifiState()
      //   getConnectionInfo()
      //   getDhcpInfo()

      // TODO do we need these:
      //   createWifiLock(string)
      //   createWifiLock(int, string)
      //   getConfiguredNetworks()
      //      for WifiConfiguration ...

      // isWifiEnabled()
      hook_method("android.net.wifi.WifiManager", lpparam.classLoader,
              "isWifiEnabled", new XC_MethodHook()
              {
                  @Override
                  protected void afterHookedMethod(MethodHookParam param) throws Throwable
                  {
                      log_call("isWifiEnabled(), " + (hack_enabled() ? "faking wifi" : "called"));
                      if (hack_enabled())
                          param.setResult(true);
                  }
              });

      // getWifiState()
      hook_method("android.net.wifi.WifiManager", lpparam.classLoader,
              "getWifiState", new XC_MethodHook()
              {
                  @Override
                  protected void afterHookedMethod(MethodHookParam param) throws Throwable
                  {
                      log_call("getWifiState(), " + (hack_enabled() ? "faking wifi" : "called"));
                      if (hack_enabled())
                          param.setResult(WifiManager.WIFI_STATE_ENABLED);
                  }
              });


      // getConnectionInfo()
      hook_method("android.net.wifi.WifiManager", lpparam.classLoader,
              "getConnectionInfo", new XC_MethodHook()
              {
                  @Override
                  protected void afterHookedMethod(MethodHookParam param) throws Throwable
                  {
                      log_call("getConnectionInfo(), " + (hack_enabled() ? "faking wifi" : "called"));
                      if (hack_enabled())
                          param.setResult(createWifiInfo());
                  }
              });

      // getDhcpInfo()
      hook_method("android.net.wifi.WifiManager", lpparam.classLoader,
              "getDhcpInfo", new XC_MethodHook()
              {
                  @Override
                  protected void afterHookedMethod(MethodHookParam param) throws Throwable
                  {
                      boolean doit = hack_enabled() && getIPInfo() != null;
                      log_call("getDhcpInfo(), " + (doit ? "faking wifi" : "called"));
                      if (doit)
                          param.setResult(createDhcpInfo());
                  }
              });


      if (isDebug()){

          // requestRouteToHost(int, int)		LOG ONLY
          hook_method("android.net.ConnectivityManager", lpparam.classLoader,
                  "requestRouteToHost", int.class, int.class, new XC_MethodHook()
                  {
                      @Override
                      protected void afterHookedMethod(MethodHookParam param) throws Throwable
                      {
                          int network_type = (Integer) param.args[0];
                          int host_addr = (Integer) param.args[1];
                          String called = "requestRouteToHost(" + network_type + ", " + host_addr + ")";

                          log_call(called + " called.");
                      }
                  });

          // getActiveLinkProperties()		LOG ONLY
          hook_method("android.net.ConnectivityManager", lpparam.classLoader,
                  "getActiveLinkProperties", new XC_MethodHook()
                  {
                      @Override
                      protected void afterHookedMethod(MethodHookParam param) throws Throwable
                      {
                          log_call("getActiveLinkProperties() called.");
                      }
                  });

          // getLinkProperties(int)			LOG ONLY
          hook_method("android.net.ConnectivityManager", lpparam.classLoader,
                  "getLinkProperties", int.class, new XC_MethodHook()
                  {
                      @Override
                      protected void afterHookedMethod(MethodHookParam param) throws Throwable
                      {
                          int network_type = (Integer) param.args[0];
                          log_call("getLinkProperties(" + network_type + ") called.");
                      }
                  });

          // *************************************************************************************
          // debug only
          // createWifiLock(string)
          hook_method("android.net.wifi.WifiManager", lpparam.classLoader,
                  "createWifiLock", String.class, new XC_MethodHook()
                  {
                      @Override
                      protected void afterHookedMethod(MethodHookParam param) throws Throwable
                      {   log_call("createWifiLock(String) called");   }
                  });

          // createWifiLock(int, string)
          hook_method("android.net.wifi.WifiManager", lpparam.classLoader,
                  "createWifiLock", int.class, String.class, new XC_MethodHook()
                  {
                      @Override
                      protected void afterHookedMethod(MethodHookParam param) throws Throwable
                      {   log_call("createWifiLock(int, String) called");    }
                  });


          // getConfiguredNetworks()
          hook_method("android.net.wifi.WifiManager", lpparam.classLoader,
                  "getConfiguredNetworks", new XC_MethodHook()
                  {
                      @Override
                      protected void afterHookedMethod(MethodHookParam param) throws Throwable
                      {   log_call("getConfiguredNetworks() called");     }
                  });

      }

  }

}

