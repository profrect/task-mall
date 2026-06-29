export interface User {
  name: string;
  vipLevel: string;
  inviteCode: string;
  kycStatus: string;
}

export interface Wallet {
  balance: number;
  frozen: number;
}

export interface Task {
  id: number;
  title: string;
  description: string;
  reward: number;
  status: 'pending' | 'in-progress' | 'completed';
}

export interface Investment {
  id: number;
  name: string;
  description: string;
  rate: number;
  amount: number;
  expectedReturn: number;
  status: 'available' | 'participated' | 'history';
}

export interface TeamMember {
  id: number;
  name: string;
  vipLevel: string;
  contribution: number;
}

export interface VipBenefit {
  id: number;
  title: string;
  description: string;
  owned: boolean;
}

export interface TransactionRecord {
  id: number;
  type: string;
  amount: number;
  time: string;
  status: string;
  to?: string;
  merchant?: string;
  user?: string;
}

export interface LoginForm { username: string; password: string }
export interface RegisterForm { username: string; password: string; inviteCode: string }
export interface ForgotForm { username: string; newPassword: string }
export interface RechargeForm { amount: string; password: string }
export interface WithdrawForm { amount: string; account: string; password: string }
export interface TransferForm { toUser: string; amount: string; password: string }
export interface PasswordChangeForm { oldPassword: string; newPassword: string; confirmPassword: string }
export interface FundPasswordChangeForm { oldPassword: string; newPassword: string; confirmPassword: string }
