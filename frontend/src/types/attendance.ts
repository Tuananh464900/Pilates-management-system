import { BookingStatus } from './booking';

export interface AttendanceRecordResponse {
  bookingId: number;
  sessionId: number;
  memberId: string;
  memberEmail: string;
  memberName: string;
  memberPhone?: string;
  status: BookingStatus;
  refunded: boolean;
  bookedAt: string;
}

export interface AttendanceItemRequest {
  bookingId: number;
  status: 'ATTENDED' | 'ABSENT';
}

export interface MarkAttendanceRequest {
  items: AttendanceItemRequest[];
  completeSession: boolean;
}
