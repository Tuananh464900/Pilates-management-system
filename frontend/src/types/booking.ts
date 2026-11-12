import { ClassLevel } from './pilates';

export type BookingStatus = 'CONFIRMED' | 'CANCELED' | 'ATTENDED' | 'ABSENT';

export interface Booking {
  id: number;
  memberId: string;
  memberEmail: string;
  memberName?: string;
  memberPhone?: string;

  // Chi tiết buổi tập
  sessionId: number;
  className: string;
  classLevel: ClassLevel;
  roomName: string;
  trainerName: string;
  startTime: string;
  endTime: string;

  // Gói tập đã dùng
  membershipId: number;
  packageName: string;

  // Trạng thái
  status: BookingStatus;
  refunded: boolean;
  cancelReason?: string;
  canceledAt?: string;
  createdAt: string;

  // Cờ nghiệp vụ
  canCancel: boolean;
  isRefundable: boolean;
}
