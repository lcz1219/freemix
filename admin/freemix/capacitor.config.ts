import type { CapacitorConfig } from '@capacitor/cli';

const config: CapacitorConfig = {
  appId: 'mobile.freemix.app',
  appName: 'FreeMix',
  webDir: 'dist',
  plugins: {
    CapacitorHttp: {
      enabled: true,
    },
    SplashScreen: {
      launchShowDuration: 3000,
      launchAutoHide: true,
      backgroundColor: "#ffffffff",
      showSpinner: false,
      androidScaleType: "FIT_CENTER",
    },
  },
  server: {
    androidScheme: 'https'
  }
};

export default config;
