/**
 * Process-related models and interfaces
 */

// =============================================================================
// Process Categories
// =============================================================================

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

/**
 * Process Instance
 */
export interface ProcessInstance {
  id: number;
  processDefinitionId: number;
  processDefinitionName: string;
  processKey: string;
  processVersion: number;
  businessKey?: string;
  status: 'RUNNING' | 'SUSPENDED' | 'COMPLETED' | 'FAILED' | 'TERMINATED';
  startTime: string;
  endTime?: string;
  durationMillis?: number;
  startedBy: string;
  currentActivityId?: string;
  currentActivityName?: string;
  errorMessage?: string;
  createdAt: string;
  updatedAt?: string;
}

/**
 * Start Process Instance Request
 */
export interface StartProcessInstanceRequest {
  processDefinitionId: number;
  businessKey?: string;
  variables?: { [key: string]: any };
}

/**
 * Execution History
 */
export interface ExecutionHistory {
  id: number;
  eventType: string;
  activityId?: string;
  activityName?: string;
  activityType?: string;
  timestamp: string;
  durationMillis?: number;
  performedBy?: string;
  eventDetails?: string;
  errorMessage?: string;
}

// =============================================================================
// Tasks
// =============================================================================

export type TaskStatus = 'CREATED' | 'ASSIGNED' | 'IN_PROGRESS' | 'COMPLETED' | 'CANCELLED';
export type TaskPriority = 'LOW' | 'NORMAL' | 'HIGH' | 'CRITICAL';
export type CommentType = 'GENERAL' | 'QUESTION' | 'DECISION' | 'ESCALATION' | 'RESOLUTION';

/**
 * Task
 */
export interface Task {
  id: number;
  name: string;
  description?: string;
  status: TaskStatus;
  priority: TaskPriority;
  assignee?: string;
  candidateGroup?: string;
  dueDate?: string;
  followUpDate?: string;
  processInstanceId?: number;
  processInstanceBusinessKey?: string;
  processDefinitionName?: string;
  activityId?: string;
  formKey?: string;
  completedDate?: string;
  completedBy?: string;
  claimedDate?: string;
  claimedBy?: string;
  commentCount: number;
  attachmentCount: number;
  isOverdue: boolean;
  isActive: boolean;
  createdAt: string;
  createdBy: string;
  updatedAt: string;
  updatedBy: string;
}

/**
 * Create Task Request
 */
export interface CreateTaskRequest {
  name: string;
  description?: string;
  priority?: TaskPriority;
  assignee?: string;
  candidateGroup?: string;
  dueDate?: string;
  followUpDate?: string;
  processInstanceId?: number;
  activityId?: string;
  formKey?: string;
}

/**
 * Update Task Request
 */
export interface UpdateTaskRequest {
  name?: string;
  description?: string;
  priority?: TaskPriority;
  dueDate?: string;
  followUpDate?: string;
}

/**
 * Task Comment
 */
export interface TaskComment {
  id: number;
  taskId: number;
  content: string;
  type?: CommentType;
  createdAt: string;
  createdBy: string;
}

/**
 * Create Comment Request
 */
export interface CreateCommentRequest {
  content: string;
  type?: CommentType;
}

/**
 * Task Attachment
 */
export interface TaskAttachment {
  id: number;
  taskId: number;
  fileName: string;
  storedFileName: string;
  filePath: string;
  mimeType: string;
  fileSize: number;
  formattedSize: string;
  description?: string;
  createdAt: string;
  createdBy: string;
}

// =============================================================================
// Forms (Dynamic Forms)
// =============================================================================

/**
 * Submission Status
 */
export type SubmissionStatus = 'DRAFT' | 'SUBMITTED' | 'APPROVED' | 'REJECTED' | 'CANCELLED';

/**
 * Form Definition
 */
export interface FormDefinition {
  id: number;
  formKey: string;
  name: string;
  description?: string;
  category?: string;
  version: number;
  schemaJson: string;
  uiSchemaJson?: string;
  published: boolean;
  isLatestVersion: boolean;
  processDefinitionKey?: string;
  taskDefinitionKey?: string;
  submissionCount: number;
  createdAt: string;
  createdBy: string;
  updatedAt: string;
  updatedBy: string;
}

/**
 * Create Form Definition Request
 */
export interface CreateFormDefinitionRequest {
  formKey: string;
  name: string;
  description?: string;
  category?: string;
  schemaJson: string;
  uiSchemaJson?: string;
  processDefinitionKey?: string;
  taskDefinitionKey?: string;
}

/**
 * Update Form Definition Request
 */
export interface UpdateFormDefinitionRequest {
  name: string;
  description?: string;
  category?: string;
  schemaJson: string;
  uiSchemaJson?: string;
  processDefinitionKey?: string;
  taskDefinitionKey?: string;
}

/**
 * Form Submission
 */
export interface FormSubmission {
  id: number;
  formDefinitionId: number;
  formKey: string;
  formName: string;
  formVersion: number;
  taskId?: number;
  processInstanceId?: number;
  dataJson: string;
  status: SubmissionStatus;
  submittedBy: string;
  submittedAt?: string;
  businessKey?: string;
  notes?: string;
  validationErrors?: string;
  createdAt: string;
  createdBy: string;
  updatedAt: string;
  updatedBy: string;
}

/**
 * Save Draft Request
 */
export interface SaveDraftRequest {
  formDefinitionId: number;
  dataJson: string;
  businessKey?: string;
  taskId?: number;
  processInstanceId?: number;
}

/**
 * Submit Form Request
 */
export interface SubmitFormRequest {
  formDefinitionId: number;
  dataJson: string;
  businessKey?: string;
  taskId?: number;
  processInstanceId?: number;
  notes?: string;
}

/**
 * Form Schema (JSON Schema Draft 7)
 */
export interface FormSchema {
  $schema?: string;
  title?: string;
  description?: string;
  type: 'object';
  properties: { [key: string]: FormSchemaProperty };
  required?: string[];
  additionalProperties?: boolean;
}

/**
 * Form Schema Property
 */
export interface FormSchemaProperty {
  type: 'string' | 'number' | 'integer' | 'boolean' | 'array' | 'object';
  title?: string;
  description?: string;
  default?: any;
  enum?: any[];
  minimum?: number;
  maximum?: number;
  minLength?: number;
  maxLength?: number;
  pattern?: string;
  format?: string; // date, date-time, email, uri, etc.
  items?: FormSchemaProperty; // for arrays
  properties?: { [key: string]: FormSchemaProperty }; // for objects
  required?: string[];
}

/**
 * UI Schema for rendering hints
 */
export interface UISchema {
  [key: string]: UISchemaElement;
}

/**
 * UI Schema Element
 */
export interface UISchemaElement {
  'ui:widget'?: string; // text, textarea, select, radio, checkbox, date, email, etc.
  'ui:placeholder'?: string;
  'ui:help'?: string;
  'ui:readonly'?: boolean;
  'ui:disabled'?: boolean;
  'ui:autofocus'?: boolean;
  'ui:options'?: {
    rows?: number;
    label?: boolean;
    inline?: boolean;
    orderable?: boolean;
    addable?: boolean;
    removable?: boolean;
  };
  'ui:order'?: string[];
}

/**
 * Validation Response
 */
export interface ValidationResponse {
  valid: boolean;
  errors?: string[];
  message?: string;
}

// ============================================================================
// Dashboard & Metrics Models
// ============================================================================

/**
 * System KPIs
 */
export interface SystemKPIs {
  activeProcesses: number;
  completedProcessesToday: number;
  failedProcessesToday: number;
  totalProcesses: number;
  activeTasks: number;
  overdueTasks: number;
  tasksCompletedToday: number;
  totalTasks: number;
  avgTaskCompletionTimeMinutes: number;
  avgProcessDurationHours: number;
  activeUsers: number;
  totalUsers: number;
}

/**
 * Status Statistics
 */
export interface StatusStats {
  stats: { [status: string]: number };
}

/**
 * User Task Statistics
 */
export interface UserTaskStats {
  assigned: number;
  completed: number;
  inProgress: number;
  overdue: number;
  avgCompletionTimeMinutes: number;
}

/**
 * Process Definition Statistics
 */
export interface ProcessDefinitionStats {
  processDefinitionKey: string;
  total: number;
  active: number;
  completed: number;
  failed: number;
  avgDurationHours: number;
}

/**
 * Daily Completion Trend
 */
export interface DailyCompletionTrend {
  dailyCompletions: { [date: string]: number };
  days: number;
}
