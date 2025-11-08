/**
 * Audit-related models and interfaces
 */

// =============================================================================
// Audit Log
// =============================================================================

/**
 * Audit Log Entry
 */
export interface AuditLog {
  id: number;
  timestamp: string;
  username: string;
  action: string;
  entityType?: string;
  entityId?: number;
  entityName?: string;
  oldValue?: string;
  newValue?: string;
  httpMethod?: string;
  requestUrl?: string;
  ipAddress?: string;
  userAgent?: string;
  sessionId?: string;
  result: string;
  errorMessage?: string;
  context?: string;
  severity: string;
  tags?: string;
}

/**
 * Audit Log Statistics
 */
export interface AuditStats {
  totalLogs: number;
  securityLogs: number;
  failedActions: number;
}

/**
 * Failed Login Count Response
 */
export interface FailedLoginCount {
  username: string;
  failedLoginCount: number;
  windowMinutes: number;
}

/**
 * Audit Severity Levels
 */
export enum AuditSeverity {
  INFO = 'INFO',
  WARNING = 'WARNING',
  ERROR = 'ERROR',
  CRITICAL = 'CRITICAL'
}

/**
 * Common Audit Actions
 */
export enum AuditAction {
  // User actions
  CREATE_USER = 'CREATE_USER',
  UPDATE_USER = 'UPDATE_USER',
  DELETE_USER = 'DELETE_USER',

  // Process actions
  CREATE_PROCESS = 'CREATE_PROCESS',
  UPDATE_PROCESS = 'UPDATE_PROCESS',
  DELETE_PROCESS = 'DELETE_PROCESS',
  START_PROCESS = 'START_PROCESS',
  SUSPEND_PROCESS = 'SUSPEND_PROCESS',
  RESUME_PROCESS = 'RESUME_PROCESS',
  TERMINATE_PROCESS = 'TERMINATE_PROCESS',

  // Task actions
  CLAIM_TASK = 'CLAIM_TASK',
  ASSIGN_TASK = 'ASSIGN_TASK',
  COMPLETE_TASK = 'COMPLETE_TASK',

  // Security actions
  LOGIN_SUCCESS = 'LOGIN_SUCCESS',
  LOGIN_FAILED = 'LOGIN_FAILED',
  LOGOUT = 'LOGOUT',
  PASSWORD_CHANGE = 'PASSWORD_CHANGE',

  // System actions
  CREATE_SYSTEM_PARAMETER = 'CREATE_SYSTEM_PARAMETER',
  UPDATE_SYSTEM_PARAMETER = 'UPDATE_SYSTEM_PARAMETER',
  UPDATE_SYSTEM_PARAMETER_VALUE = 'UPDATE_SYSTEM_PARAMETER_VALUE',
  DELETE_SYSTEM_PARAMETER = 'DELETE_SYSTEM_PARAMETER',
  RESET_SYSTEM_PARAMETER = 'RESET_SYSTEM_PARAMETER'
}
