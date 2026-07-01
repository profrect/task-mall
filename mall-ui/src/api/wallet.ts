import { http } from './http';

/** 钱包总览（对齐后端 WalletAccountVO）。 */
export interface WalletOverview {
  userId: number;
  currency: string;
  totalBalance: number;
  availBalance: number;
  frozenBalance: number;
}

/** 充值收款地址（对齐后端 DepositAddressVO）。 */
export interface DepositAddress {
  chainCode: string;
  address: string;
}

/** 充值记录（对齐后端 RechargeOrderVO）。 */
export interface RechargeRecord {
  orderNo: string;
  chainCode: string;
  coin: string;
  amount: number;
  fromAddress?: string;
  toAddress: string;
  txHash?: string;
  confirmations: number;
  status: string;
  creditedAt?: number;
  createTime: number;
}

/** 支持的充值链。阶段1仅 TRON 真正可用，其余阶段2接入 EVM 适配器。 */
export type ChainCode = 'TRON' | 'ETH' | 'BSC' | 'POLYGON';

const BASE = '/api/wallet';

/** 钱包总览：首次访问即懒初始化账户。 */
export function getOverview(): Promise<WalletOverview> {
  return http.get<WalletOverview>(`${BASE}/overview`);
}

/** 取本人在指定链的收款地址（无则后端即时分配）。 */
export function getDepositAddress(chain: ChainCode = 'TRON'): Promise<DepositAddress> {
  return http.get<DepositAddress>(`${BASE}/deposit/address`, { chain });
}

/** 本人充值记录（时间倒序）。 */
export function getRechargeRecords(): Promise<RechargeRecord[]> {
  return http.get<RechargeRecord[]>(`${BASE}/recharge/records`);
}

/** 提现申请入参（对齐后端 WithdrawApplyDTO）。userId 由会话解析，不传；amount 含手续费。 */
export interface WithdrawApply {
  chain?: ChainCode;
  coin?: string;
  toAddress: string;
  amount: number;
}

/** 提现记录（对齐后端 WithdrawOrderVO）。金额三元组：amount=申请额、fee=手续费、receiveAmount=到账额。 */
export interface WithdrawRecord {
  orderNo: string;
  chainCode: string;
  coin: string;
  amount: number;
  fee: number;
  receiveAmount: number;
  toAddress: string;
  txHash?: string;
  confirmations: number;
  status: string;
  reviewRemark?: string;
  reviewedAt?: number;
  broadcastAt?: number;
  finishedAt?: number;
  createTime: number;
}

/** 账务流水（对齐后端 WalletFlowResp）。changeAmt 恒为正，direction 表达资金方向。 */
export interface WalletFlowRecord {
  flowNo: string;
  userId: number;
  bizType: string;
  bizId: string;
  direction: string;
  changeAmt: number;
  balanceBefore: number;
  balanceAfter: number;
  remark?: string;
  createTime: number;
}

/** 站内转账申请入参。fromUserId 由会话解析，不传。 */
export interface TransferApply {
  toUserId: number;
  coin?: string;
  amount: number;
  remark?: string;
}

/** 站内转账记录（转出/转入均会返回）。 */
export interface TransferRecord {
  orderNo: string;
  fromUserId: number;
  toUserId: number;
  coin: string;
  amount: number;
  status: string;
  remark?: string;
  finishedAt?: number;
  createTime: number;
}

/** 申请提现：冻结可用余额并落单，进入人工审核。返回新建的提现订单。 */
export function applyWithdraw(data: WithdrawApply): Promise<WithdrawRecord> {
  return http.post<WithdrawRecord>(`${BASE}/withdraw/apply`, data);
}

/** 本人提现记录（时间倒序）。 */
export function getWithdrawRecords(): Promise<WithdrawRecord[]> {
  return http.get<WithdrawRecord[]>(`${BASE}/withdraw/records`);
}

/** 站内转账：同事务扣转出方、加转入方，并写双方流水。 */
export function applyTransfer(data: TransferApply): Promise<TransferRecord> {
  return http.post<TransferRecord>(`${BASE}/transfer/apply`, data);
}

/** 本人站内转账记录（时间倒序，包含转出/转入）。 */
export function getTransferRecords(): Promise<TransferRecord[]> {
  return http.get<TransferRecord[]>(`${BASE}/transfer/records`);
}

/** 本人账务流水（时间倒序）。 */
export function getWalletFlows(limit = 200): Promise<WalletFlowRecord[]> {
  return http.get<WalletFlowRecord[]>(`${BASE}/flows`, { limit });
}