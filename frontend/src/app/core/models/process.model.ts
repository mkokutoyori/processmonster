/**
 * Process-related models and interfaces
 */

/**
 * Process Category
 */
export interface ProcessCategory {
  id: number;
  code: string;
  name: string;
  description?: string;
  icon?: string;
  color?: string;
  displayOrder: number;
  active: boolean;
  createdAt: string;
  updatedAt?: string;
  createdBy?: string;
  updatedBy?: string;
  processCount?: number;
}

/**
 * Create Process Category Request
 */
export interface CreateProcessCategoryRequest {
  code: string;
  name: string;
  description?: string;
  icon?: string;
  color?: string;
  displayOrder?: number;
  active?: boolean;
}

/**
 * Update Process Category Request
 */
export interface UpdateProcessCategoryRequest {
  code?: string;
  name?: string;
  description?: string;
  icon?: string;
  color?: string;
  displayOrder?: number;
  active?: boolean;
}

/**
 * Process Definition (summary)
 */
export interface ProcessDefinition {
  id: number;
  processKey: string;
  name: string;
  version: number;
  isLatestVersion: boolean;
  description?: string;
  isTemplate: boolean;
  published: boolean;
  active: boolean;
  tags?: string;
  deployedAt?: string;
  deployedBy?: string;
  createdAt: string;
  updatedAt?: string;
  createdBy?: string;
  updatedBy?: string;
  category?: ProcessCategory;
  tagList?: string[];
  fullIdentifier: string;
  deployed: boolean;
}

/**
 * Process Definition Detail (with BPMN XML)
 */
export interface ProcessDefinitionDetail extends ProcessDefinition {
  bpmnXml: string;
}

/**
 * Process Version Info (simplified for version listing)
 */
export interface ProcessVersionInfo {
  id: number;
  processKey: string;
  name: string;
  version: number;
  isLatestVersion: boolean;
  published: boolean;
  active: boolean;
  deployed: boolean;
  createdAt: string;
  createdBy?: string;
  deployedAt?: string;
  deployedBy?: string;
  fullIdentifier: string;
}

/**
 * Create Process Definition Request
 */
export interface CreateProcessDefinitionRequest {
  name: string;
  bpmnXml: string;
  description?: string;
  categoryId?: number;
  isTemplate?: boolean;
  published?: boolean;
  active?: boolean;
  tags?: string;
}

/**
 * Update Process Definition Request
 */
export interface UpdateProcessDefinitionRequest {
  name?: string;
  bpmnXml?: string;
  description?: string;
  categoryId?: number;
  isTemplate?: boolean;
  published?: boolean;
  active?: boolean;
  tags?: string;
}

/**
 * Import BPMN Request
 */
export interface ImportBpmnRequest {
  bpmnXml: string;
  categoryId?: number;
  asTemplate?: boolean;
}

/**
 * Paginated response
 */
export interface PagedResponse<T> {
  content: T[];
  pageable: {
    pageNumber: number;
    pageSize: number;
    sort: {
      sorted: boolean;
      unsorted: boolean;
      empty: boolean;
    };
    offset: number;
    paged: boolean;
    unpaged: boolean;
  };
  totalElements: number;
  totalPages: number;
  last: boolean;
  size: number;
  number: number;
  sort: {
    sorted: boolean;
    unsorted: boolean;
    empty: boolean;
  };
  first: boolean;
  numberOfElements: number;
  empty: boolean;
}
