import { getApp, getApps, initializeApp } from 'firebase/app';
import { connectAuthEmulator, getAuth } from 'firebase/auth';
import { connectFunctionsEmulator, getFunctions } from 'firebase/functions';

const config = {
  apiKey: process.env.NEXT_PUBLIC_FIREBASE_API_KEY,
  authDomain: process.env.NEXT_PUBLIC_FIREBASE_AUTH_DOMAIN,
  projectId: process.env.NEXT_PUBLIC_FIREBASE_PROJECT_ID,
  appId: process.env.NEXT_PUBLIC_FIREBASE_APP_ID,
};

export const isFirebaseConfigured = Object.values(config).every(
  (value) => typeof value === 'string' && value.length > 0
);

const app = isFirebaseConfigured
  ? getApps().length > 0
    ? getApp()
    : initializeApp(config)
  : null;

export const firebaseAuth = app ? getAuth(app) : null;
export const firebaseFunctions = app
  ? getFunctions(app, process.env.NEXT_PUBLIC_FIREBASE_FUNCTIONS_REGION || 'us-central1')
  : null;

if (
  app &&
  process.env.NEXT_PUBLIC_FIREBASE_EMULATOR === 'true' &&
  typeof window !== 'undefined'
) {
  const emulatorHost = process.env.NEXT_PUBLIC_FIREBASE_EMULATOR_HOST || '127.0.0.1';
  connectAuthEmulator(firebaseAuth!, `http://${emulatorHost}:9099`, { disableWarnings: true });
  connectFunctionsEmulator(firebaseFunctions!, emulatorHost, 5001);
}
