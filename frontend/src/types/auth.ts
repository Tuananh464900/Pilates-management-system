export type Role = 'ADMIN' | 'TRAINER' | 'MEMBER';
export type UserStatus = 'ACTIVE' | 'LOCKED';
export type Gender = 'NAM' | 'NU' | 'KHAC';

export interface User {
  id: string;
  email: string;
  role: Role;
  status: UserStatus;
  fullName?: string;
  phone?: string;
  createdAt?: string;
}

export interface MemberProfile {
  userId: string;
  email: string;
  fullName: string;
  phone: string;
  gender?: Gender;
  dateOfBirth?: string;
  address?: string;
  status: UserStatus;
  createdAt: string;
}

export interface TrainerProfile {
  userId: string;
  email: string;
  fullName: string;
  phone: string;
  specialization?: string;
  bio?: string;
  status: UserStatus;
  createdAt: string;
}

export interface ApiResponse<T> {
  success: boolean;
  message: string;
  data: T;
  timestamp: string;
}

export interface PageResponse<T> {
  items: T[];
  page: number;
  size: number;
  totalElements: number;
  totalPages: number;
  last: boolean;
}
