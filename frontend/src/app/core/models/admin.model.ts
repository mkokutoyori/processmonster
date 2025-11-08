/**
 * Admin-related models and interfaces
 */

// =============================================================================
// System Parameters
// =============================================================================

/**
 * System Parameter
 */
export interface SystemParameter {
  id: number;
  key: string;
  value?: string;
  description?: string;
  category: string;
  dataType: SystemParameterDataType;
  defaultValue?: string;
  encrypted: boolean;
  editable: boolean;
  validationPattern?: string;
  allowedValues?: string;
  displayOrder: number;
  createdAt: string;
  updatedAt?: string;
}

/**
 * Create System Parameter Request
 */
export interface CreateSystemParameterRequest {
  key: string;
  value?: string;
  description?: string;
  category?: string;
  dataType?: SystemParameterDataType;
  defaultValue?: string;
  encrypted?: boolean;
  editable?: boolean;
  validationPattern?: string;
  allowedValues?: string;
  displayOrder?: number;
}

/**
 * Update System Parameter Request
 */
export interface UpdateSystemParameterRequest {
  description?: string;
  category?: string;
  validationPattern?: string;
  allowedValues?: string;
  displayOrder?: number;
  editable?: boolean;
}

/**
 * Update System Parameter Value Request
 */
export interface UpdateSystemParameterValueRequest {
  value: string;
}

/**
 * System Configuration Map
 */
export interface SystemConfiguration {
  [key: string]: string;
}

/**
 * Admin Statistics
 */
export interface AdminStats {
  totalParameters: number;
  totalCategories: number;
  editableParameters: number;
  encryptedParameters: number;
}

/**
 * System Parameter Data Types
 */
export enum SystemParameterDataType {
  STRING = 'STRING',
  INTEGER = 'INTEGER',
  LONG = 'LONG',
  DOUBLE = 'DOUBLE',
  BOOLEAN = 'BOOLEAN'
}

/**
 * Common System Parameter Categories
 */
export enum SystemParameterCategory {
  GENERAL = 'General',
  SECURITY = 'Security',
  PERFORMANCE = 'Performance',
  INTEGRATION = 'Integration',
  NOTIFICATIONS = 'Notifications',
  STORAGE = 'Storage',
  EMAIL = 'Email',
  AUDIT = 'Audit',
  WORKFLOW = 'Workflow'
}
