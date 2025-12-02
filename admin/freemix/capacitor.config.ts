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
      backgroundColor: "#ffffff",
      showSpinner: false,
      androidScaleType: "FIT_CENTER",
      iosLaunchImage: "Splash",
      iosLaunchImageDark: "Splash",
      fadeOutDuration: 1000,
      fadeInDuration: 1000,
      androidSplashResourceName: "splash",
    },
  },
  server: {
    androidScheme: 'https'
  }
};

export default config;
