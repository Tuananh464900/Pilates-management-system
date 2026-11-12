export type ClassLevel = 'BEGINNER' | 'INTERMEDIATE' | 'ADVANCED' | 'ALL_LEVELS';
export type ClassStatus = 'ACTIVE' | 'INACTIVE';

export interface PilatesClass {
  id: number;
  name: string;
  description?: string;
  defaultCapacity: number;
  level: ClassLevel;
  status: ClassStatus;
  createdAt: string;
}

export type RoomStatus = 'ACTIVE' | 'INACTIVE';

export interface Room {
  id: number;
  name: string;
  capacity: number;
  description?: string;
  status: RoomStatus;
  createdAt: string;
}

export type SessionStatus = 'SCHEDULED' | 'COMPLETED' | 'CANCELED';

export interface Session {
  id: number;
  classId: number;
  className: string;
  classLevel: ClassLevel;
  trainerId: string;
  trainerName: string;
  trainerPhone?: string;
  roomId: number;
  roomName: string;
  startTime: string;
  endTime: string;
  capacity: number;
  bookedCount: number;
  availableCount: number;
  status: SessionStatus;
  cancelReason?: string;
  createdAt: string;
}

export type SessionResponse = Session;
