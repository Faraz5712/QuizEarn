import { httpsCallable } from 'firebase/functions';
import { firebaseFunctions, isFirebaseConfigured } from './firebase';

export type AdminResource =
  | 'categories'
  | 'questions'
  | 'tasks'
  | 'tournaments'
  | 'rewards'
  | 'users'
  | 'redemptions';

function callable<TRequest extends Record<string, unknown>, TResponse>(
  name: string,
  data: TRequest
): Promise<TResponse> {
  if (!firebaseFunctions) {
    throw new Error('Firebase Admin backend is not configured.');
  }
  return httpsCallable<TRequest, TResponse>(firebaseFunctions, name)(data).then(
    (response) => response.data
  );
}

export function listAdminRecords<T>(resource: AdminResource): Promise<T[]> {
  if (!isFirebaseConfigured) throw new Error('Firebase Admin backend is not configured.');
  return callable<{ resource: AdminResource }, { records: T[] }>(
    'adminListRecords',
    { resource }
  ).then((response) => response.records);
}

export function saveAdminRecord<T extends Record<string, unknown>>(
  resource: AdminResource,
  record: T
): Promise<{ id: string }> {
  return callable('adminSaveRecord', { resource, record });
}

export function deleteAdminRecord(
  resource: AdminResource,
  id: string
): Promise<{ deleted: boolean }> {
  return callable('adminDeleteRecord', { resource, id });
}

export function adjustAdminRewardStock(
  rewardId: string,
  stockCount: number
): Promise<{ rewardId: string; stockCount: number }> {
  return callable('adminAdjustRewardStock', { rewardId, stockCount });
}

export function processAdminRedemption(
  redemptionId: string,
  decision: 'APPROVE' | 'REJECT',
  details: { digitalCode?: string; reason?: string }
): Promise<{ redemption: Record<string, unknown> }> {
  return callable('processRedemption', { redemptionId, decision, ...details });
}

export function cancelAdminTournament(
  tournamentId: string,
  reason: string
): Promise<{ tournamentId: string; alreadyCancelled: boolean }> {
  return callable('cancelTournament', { tournamentId, reason });
}

export function finalizeAdminTournament(
  tournamentId: string
): Promise<{ tournamentId: string; alreadyFinalized: boolean }> {
  return callable('finalizeTournament', { tournamentId });
}
