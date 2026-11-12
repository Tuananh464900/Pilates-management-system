export type PackageStatus = 'ACTIVE' | 'INACTIVE';

export interface MembershipPackage {
  id: number;
  name: string;
  description?: string;
  price: number;
  totalSessions: number;
  durationDays: number;
  status: PackageStatus;
  createdAt: string;
}

export type PaymentStatus = 'UNPAID' | 'PAID' | 'CANCELED';
export type PaymentMethod = 'CASH' | 'BANK_TRANSFER';

export interface Payment {
  id: number;
  transactionCode: string;
  memberId: string;
  memberEmail: string;
  memberName?: string;
  packageId: number;
  packageName: string;
  membershipId?: number;
  amount: number;
  paymentMethod: PaymentMethod;
  status: PaymentStatus;
  paidAt?: string;
  note?: string;
  createdAt: string;
}

export type MembershipStatus = 'ACTIVE' | 'EXPIRED' | 'EXHAUSTED' | 'CANCELED';

export interface MemberMembership {
  id: number;
  memberId: string;
  memberEmail: string;
  memberName?: string;
  packageId: number;
  packageName: string;
  totalSessions: number;
  remainingSessions: number;
  startDate: string;
  endDate: string;
  daysRemaining: number;
  status: MembershipStatus;
  warningDays: boolean;
  warningSessions: boolean;
  createdAt: string;
}
