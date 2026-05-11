import { LitElement, html, css } from 'lit';
import { classMap } from 'lit/directives/class-map.js';
import { TemplateService } from '../api-service.js';

export class PriceAlertApp extends LitElement {
  static styles = css`
    :host {
      display: grid;
      grid-template-columns: 320px 1fr;
      gap: 0;
      min-height: 100vh;
      font-family: 'Segoe UI', Roboto, sans-serif;
      background-color: #f4f7f6;
      position: relative;
    }
    .sidebar {
      background: white;
      border-right: 1px solid #e0e0e0;
      padding: 20px;
      box-shadow: 2px 0 5px rgba(0,0,0,0.05);
      display: flex;
      flex-direction: column;
    }
    .content {
      padding: 40px;
      background: white;
      margin: 20px;
      border-radius: 8px;
      box-shadow: 0 2px 10px rgba(0,0,0,0.1);
    }
    .template-list {
      flex-grow: 1;
      overflow-y: auto;
    }
    .template-item {
      padding: 15px;
      margin-bottom: 10px;
      border: 1px solid #eee;
      border-radius: 6px;
      cursor: pointer;
      transition: all 0.2s;
      position: relative;
    }
    .template-item:hover { border-color: #007bff; background: #f8fbff; }
    .template-item.selected { border-color: #007bff; background: #e7f3ff; font-weight: bold; }
    
    .delete-item-btn {
      position: absolute;
      right: 10px;
      top: 50%;
      transform: translateY(-50%);
      background: #dc3545;
      color: white;
      border: none;
      border-radius: 4px;
      padding: 4px 8px;
      font-size: 11px;
      cursor: pointer;
      opacity: 0;
      transition: opacity 0.2s;
    }
    .template-item:hover .delete-item-btn {
      opacity: 1;
    }
    .delete-item-btn:hover {
      background: #c82333;
    }
    
    .form-group { margin-bottom: 25px; }
    label { display: block; font-weight: 600; margin-bottom: 8px; color: #444; }
    
    input, textarea, select {
      width: 100%;
      padding: 10px;
      border: 1px solid #ccc;
      border-radius: 4px;
      font-size: 14px;
      box-sizing: border-box;
      transition: all 0.2s;
    }

    input.invalid, select.invalid, textarea.invalid {
      border: 2px solid #dc3545 !important;
      background-color: #fff8f8;
    }

    textarea { min-height: 120px; resize: vertical; }
    .row-item {
      display: flex;
      gap: 10px;
      margin-bottom: 10px;
      align-items: center;
      padding: 10px;
      background: #fafafa;
      border-radius: 4px;
    }
    button {
      padding: 10px 20px;
      border-radius: 4px;
      border: none;
      cursor: pointer;
      font-weight: 600;
      transition: opacity 0.2s;
    }
    button:disabled {
      opacity: 0.6;
      cursor: not-allowed;
    }
    .btn-main {
      background: #007bff;
      color: white;
    }
    .btn-main:disabled {
      background-color: #ccc;
    }
    .btn-secondary { background: #6c757d; color: white; }
    .btn-danger { background: #dc3545; color: white; padding: 8px 12px; }
    button:hover { opacity: 0.9; }
    hr { margin: 30px 0; border: 0; border-top: 1px solid #eee; }

    .pagination-controls {
      padding: 15px 0;
      display: flex;
      justify-content: space-between;
      align-items: center;
      border-top: 1px solid #eee;
      margin-top: auto;
    }

    /* Loading Spinner */
    .spinner-overlay {
      position: fixed;
      top: 0;
      left: 0;
      right: 0;
      bottom: 0;
      background: rgba(0,0,0,0.5);
      display: flex;
      justify-content: center;
      align-items: center;
      z-index: 1000;
    }
    .spinner {
      width: 50px;
      height: 50px;
      border: 3px solid #f3f3f3;
      border-top: 3px solid #007bff;
      border-radius: 50%;
      animation: spin 1s linear infinite;
    }
    @keyframes spin {
      0% { transform: rotate(0deg); }
      100% { transform: rotate(360deg); }
    }

    /* Notification Toast */
    .notification {
      position: fixed;
      bottom: 20px;
      right: 20px;
      padding: 12px 20px;
      border-radius: 8px;
      color: white;
      font-weight: 500;
      z-index: 1000;
      animation: slideIn 0.3s ease-out;
      max-width: 400px;
      box-shadow: 0 2px 10px rgba(0,0,0,0.2);
    }
    .notification.success { background: #28a745; }
    .notification.error { background: #dc3545; }
    .notification.info { background: #17a2b8; }
    
    @keyframes slideIn {
      from { transform: translateX(100%); opacity: 0; }
      to { transform: translateX(0); opacity: 1; }
    }
  `;

  static properties = {
    templates: { type: Array },
    selectedTemplate: { type: Object },
    originalTemplate: { type: String },
    metalOptions: { type: Array },
    currentPage: { type: Number },
    totalPages: { type: Number },
    currentSearchQuery: { type: String },
    isLoading: { type: Boolean },
    notification: { type: Object },
    deleteConfirmation: { type: Object }
  };
    
  constructor() {
    super();
    this.templates = [];
    this.selectedTemplate = null;
    this.originalTemplate = null;
    this.metalOptions = [];
    this.currentPage = 0;
    this.totalPages = 1;
    this.emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
    this.currentSearchQuery = '';
    this.isLoading = false;
    this.notification = null;
    this.deleteConfirmation = null;
  }

  connectedCallback() {
    super.connectedCallback();
    this.loadSupportedItems();
    this.loadTemplates();
  }

  // NOTIFICATION METHODS
  showNotification(message, type = 'success') {
    this.notification = { message, type };
    this.requestUpdate();
    setTimeout(() => {
      this.notification = null;
      this.requestUpdate();
    }, 3000);
  }

  // LOADING STATE
  async withLoading(operation) {
    this.isLoading = true;
    this.requestUpdate();
    try {
      const result = await operation();
      return result;
    } finally {
      this.isLoading = false;
      this.requestUpdate();
    }
  }

  async loadTemplates(page = 0, size = 10) {
    await this.withLoading(async () => {
      try {
        const pageData = await TemplateService.getAll(page, size, this.currentSearchQuery);
        this.templates = pageData.content || [];
        this.totalPages = pageData.totalPages || 1;
        this.currentPage = pageData.number || 0;
        this.requestUpdate();
      } catch (e) {
        console.error("Błąd ładowania:", e);
        this.showNotification('Failed to load templates', 'error');
      }
    });
  }    

  async loadSupportedItems() {
    try {
      this.metalOptions = await TemplateService.getSupportedItems();
    } catch (e) {
      this.metalOptions = ['gold', 'silver', 'platinum'];
    }
  }

  _selectTemplate(template) {
    if (this.deleteConfirmation) return;
    this.selectedTemplate = JSON.parse(JSON.stringify(template));
    this.originalTemplate = JSON.stringify(template);
  }

  _createNew() {
    if (this.deleteConfirmation) return;
    const fresh = { 
      title: '', 
      content: '', 
      recipients: [{email: ''}], 
      rules: [{ operator: 'ITEM_IS', operand: '' }] 
    };
    this.selectedTemplate = fresh;
    this.originalTemplate = JSON.stringify(fresh);
  }

  async _deleteTemplate(template, event) {
    event.stopPropagation();
    
    // Show confirmation dialog
    this.deleteConfirmation = { template, show: true };
    this.requestUpdate();
  }

  async _confirmDelete() {
    const template = this.deleteConfirmation.template;
    this.deleteConfirmation = null;
    
    await this.withLoading(async () => {
      try {
        // Call delete API
        const response = await fetch(`http://localhost:8080/api/templates/${template.id}`, {
          method: 'DELETE'
        });
        
        if (!response.ok) throw new Error('Delete failed');
        
        this.showNotification(`Template "${template.title}" deleted successfully`, 'success');
        
        // Clear selection if deleted template was selected
        if (this.selectedTemplate?.id === template.id) {
          this.selectedTemplate = null;
          this.originalTemplate = null;
        }
        
        // Reload templates
        await this.loadTemplates(this.currentPage);
      } catch (e) {
        console.error("Delete Error:", e);
        this.showNotification('Failed to delete template', 'error');
      }
    });
  }

  _cancelDelete() {
    this.deleteConfirmation = null;
    this.requestUpdate();
  }

  _addRule() {
    this.selectedTemplate.rules = [...(this.selectedTemplate.rules || []), { operator: 'ITEM_IS', operand: '' }];
    this.requestUpdate();
  }

  _removeRule(index) {
    this.selectedTemplate.rules.splice(index, 1);
    this.selectedTemplate.rules = [...this.selectedTemplate.rules];
    this.requestUpdate();
  }

  _addRecipient() {
    this.selectedTemplate.recipients = [...(this.selectedTemplate.recipients || []), { email: '' }];
    this.requestUpdate();
  }

  _removeRecipient(index) {
    this.selectedTemplate.recipients.splice(index, 1);
    this.selectedTemplate.recipients = [...this.selectedTemplate.recipients];
    this.requestUpdate();
  }

  _handleSearch(e) {
    const query = e.target.value;
    clearTimeout(this._searchTimer);
    this._searchTimer = setTimeout(() => {
        this.currentSearchQuery = query;
        this.loadTemplates(0, 10);
    }, 300);
  }

  _goHome() {
    this.selectedTemplate = null;
    this.originalTemplate = null;
    const searchInput = this.shadowRoot.querySelector('input[placeholder*="Search by title"]');
    if (searchInput && searchInput.value) {
        searchInput.value = '';
        this.currentSearchQuery = '';
        this.loadTemplates(0, 10);
    }
  }

  async _save() {
    if (!this._isFormValid() || !this._isDirty()) return;
    
    await this.withLoading(async () => {
      try {
        if (this.selectedTemplate.id) {
          await TemplateService.update(this.selectedTemplate.id, this.selectedTemplate);
          this.showNotification('Template updated successfully!', 'success');
        } else {
          await TemplateService.save(this.selectedTemplate);
          this.showNotification('Template created successfully!', 'success');
        }
        
        await this.loadTemplates(this.currentPage);
        this.selectedTemplate = null;
        this.originalTemplate = null;
      } catch (e) {
        console.error("Save Error:", e);
        let errorMsg = 'Could not save configuration.';
        if (e.message) errorMsg += ' ' + e.message;
        this.showNotification(errorMsg, 'error');
      }
    });
  }

  _isEmailValid(email) {
    return this.emailRegex.test(email);
  }

  _isDirty() {
    if (!this.selectedTemplate || !this.originalTemplate) return false;
    return JSON.stringify(this.selectedTemplate) !== this.originalTemplate;
  }

  _isRuleValid(rule) {
    if (!rule.operand) return false;
    if (rule.operator?.startsWith('ITEM')) {
      return rule.operand && this.metalOptions.includes(rule.operand);
    } else {
      const num = parseFloat(rule.operand);
      return !isNaN(num) && isFinite(num) && num >= 0;
    }
  }

  _isFormValid() {
    const t = this.selectedTemplate;
    if (!t) return false;
    
    const titleOk = !!t.title?.trim();
    const contentOk = !!t.content?.trim();
    const emailsOk = t.recipients?.length > 0 && t.recipients.every(r => this._isEmailValid(r.email));
    const rulesOk = t.rules?.length > 0 && t.rules.every(rule => this._isRuleValid(rule));
    
    return titleOk && contentOk && emailsOk && rulesOk;
  }

  render() {
    const t = this.selectedTemplate;
    const saveDisabled = !this._isFormValid() || !this._isDirty() || this.isLoading;

    return html`
      ${this.isLoading ? html`
        <div class="spinner-overlay">
          <div class="spinner"></div>
        </div>
      ` : ''}
      
      ${this.notification ? html`
        <div class="notification ${this.notification.type}">
          ${this.notification.message}
        </div>
      ` : ''}
      
      ${this.deleteConfirmation ? html`
        <div class="spinner-overlay" style="background: rgba(0,0,0,0.7);" @click=${this._cancelDelete}>
          <div style="background: white; padding: 30px; border-radius: 8px; max-width: 400px; text-align: center;" @click=${e => e.stopPropagation()}>
            <h3>Confirm Delete</h3>
            <p>Are you sure you want to delete template:<br><strong>"${this.deleteConfirmation.template.title}"</strong>?</p>
            <div style="display: flex; gap: 10px; justify-content: center; margin-top: 20px;">
              <button class="btn-secondary" @click=${this._cancelDelete}>Cancel</button>
              <button class="btn-danger" @click=${this._confirmDelete}>Delete</button>
            </div>
          </div>
        </div>
      ` : ''}
      
      <div class="sidebar">
        <h2 @click=${this._goHome} 
        style="color: #007bff; cursor: pointer; user-select: none;" 
        title="Home page">
        Price Notifier
     </h2>
        <button class="btn-main" @click=${this._createNew} ?disabled=${this.isLoading} style="margin-bottom: 25px;">+ Create New Alert</button>
         
        <div style="padding: 0 10px 15px 10px;">
            <input type="text" 
                   placeholder="Search by title..." 
                   @input=${this._handleSearch}
                   ?disabled=${this.isLoading}
                   style="width: 100%; padding: 10px; border-radius: 20px; border: 1px solid #ccc; outline: none;">
        </div>


         <div style="display: flex; justify-content: space-between; align-items: center; margin: 0 10px 10px 10px;">
        <div style="font-weight: 600; color: #666; font-size: 13px;">Templates (sorted by title)</div>
        <div style="font-size: 11px; color: #999;">⬆️ A-Z</div>
        </div>
         
        
        <div class="template-list">
          ${this.templates.map(temp => html`
            <div class="template-item ${t && t.id === temp.id ? 'selected' : ''}" @click=${() => this._selectTemplate(temp)}>
              <div style="font-weight: bold;">${temp.title}</div>
              <div style="font-size: 12px; color: #666;">
                ${temp.recipients?.length || 0} recipients | ${temp.rules?.length || 0} rules
              </div>
              <button class="delete-item-btn" @click=${(e) => this._deleteTemplate(temp, e)}>Delete</button>
            </div>
          `)}
        </div>

        <div class="pagination-controls">
            <button class="btn-secondary" ?disabled="${this.currentPage === 0 || this.isLoading}" @click="${() => this.loadTemplates(this.currentPage - 1)}"> < </button>
            <span style="font-size: 12px; font-weight: bold;">${this.currentPage + 1} / ${this.totalPages}</span>
            <button class="btn-secondary" ?disabled="${this.currentPage >= (this.totalPages - 1) || this.isLoading}" @click="${() => this.loadTemplates(this.currentPage + 1)}"> > </button>
        </div>
      </div>
      
      <div class="content">
        ${t ? html`
          <h2>${t.id ? 'Edit Alert' : 'New Price Alert'}</h2>
          
          <div class="form-group">
            <label>Template Name</label>
            <input class="${classMap({invalid: !t.title?.trim()})}" 
                   .value=${t.title} 
                   @input=${e => { t.title = e.target.value; this.requestUpdate(); }}
                   placeholder="e.g., Gold Drop Notification"
                   ?disabled=${this.isLoading}>
          </div>

          <div class="form-group">
            <label>Email Recipients</label>
            ${t.recipients?.map((r, i) => html`
              <div class="row-item">
                <input type="email" 
                       class="${classMap({invalid: !this._isEmailValid(r.email)})}"
                       .value=${r.email} 
                       @input=${e => { r.email = e.target.value; this.requestUpdate(); }}
                       placeholder="client@example.com"
                       ?disabled=${this.isLoading}>
                <button class="btn-danger" @click=${() => this._removeRecipient(i)} ?disabled=${this.isLoading}>Remove</button>
              </div>
            `)}
            <button class="btn-secondary" @click=${this._addRecipient} ?disabled=${this.isLoading} style="padding: 5px 12px; font-size: 13px;">+ Add Recipient</button>
          </div>

          <div class="form-group">
            <label>Message Template</label>
            <textarea 
              class="${classMap({invalid: !t.content?.trim()})}"
              .value=${t.content || ''}
              @input=${e => { t.content = e.target.value; this.requestUpdate(); }} 
              placeholder="Write your alert message here..."
              ?disabled=${this.isLoading}>
            </textarea>
          </div>

          <h3>Trigger Rules (Logic AND)</h3>
          ${t.rules?.map((rule, i) => html`
            <div class="row-item">
              <select @change=${e => { 
                const newOp = e.target.value;
                const oldOp = rule.operator;
                if (newOp.startsWith('ITEM') !== oldOp?.startsWith('ITEM')) {
                    rule.operand = '';
                }
                rule.operator = newOp; 
                this.requestUpdate(); 
              }} ?disabled=${this.isLoading}>
                <option value="ITEM_IS" ?selected=${rule.operator === 'ITEM_IS'}>Item is</option>
                <option value="ITEM_IS_NOT" ?selected=${rule.operator === 'ITEM_IS_NOT'}>Item is not</option>
                <option value="PRICE_EQUAL" ?selected=${rule.operator === 'PRICE_EQUAL'}>Price is equal to</option>
                <option value="PRICE_GREATER_OR_EQUAL" ?selected=${rule.operator === 'PRICE_GREATER_OR_EQUAL'}>Price is greater than or equal to</option>
                <option value="PRICE_GREATER" ?selected=${rule.operator === 'PRICE_GREATER'}>Price is greater than</option>
                <option value="PRICE_LESS" ?selected=${rule.operator === 'PRICE_LESS'}>Price is less than</option>
                <option value="PRICE_LESS_OR_EQUAL" ?selected=${rule.operator === 'PRICE_LESS_OR_EQUAL'}>Price is less than or equal to</option>
              </select>

              ${rule.operator?.startsWith('ITEM') ? html`
                <select class="${classMap({invalid: !this._isRuleValid(rule)})}"
                        @change=${e => { rule.operand = e.target.value; this.requestUpdate(); }}
                        ?disabled=${this.isLoading}>
                  <option value="">-- select metal --</option>
                  ${this.metalOptions.map(m => html`
                    <option value="${m}" ?selected=${rule.operand === m}>${m}</option>
                  `)}
                </select>
              ` : html`
                <input type="number" step="0.01" min="0"
                       class="${classMap({invalid: !this._isRuleValid(rule)})}"
                       .value=${rule.operand} 
                       @input=${e => { rule.operand = e.target.value; this.requestUpdate(); }}
                       ?disabled=${this.isLoading}>
              `}
              <button class="btn-danger" @click=${() => this._removeRule(i)} ?disabled=${this.isLoading}>Remove</button>
            </div>
          `)}
          <button class="btn-secondary" @click=${this._addRule} ?disabled=${this.isLoading} style="padding: 5px 12px; font-size: 13px;">+ Add Rule</button>

          <hr>
          <button class="btn-main" ?disabled=${saveDisabled} @click=${this._save} style="padding: 15px; font-size: 1.1em;">
            ${this.isLoading ? 'Saving...' : (t.id ? 'Save Changes' : 'Create Alert')}
          </button>
        ` : html`
          <div style="text-align: center; color: #aaa; margin-top: 150px;">
            <svg width="64" height="64" fill="currentColor" viewBox="0 0 16 16" style="margin-bottom: 20px; opacity: 0.3;">
              <path d="M8 16a2 2 0 0 0 2-2H6a2 2 0 0 0 2 2zM8 1.918l-.797.161A4.002 4.002 0 0 0 4 6c0 .628-.134 2.197-.459 3.742-.16.767-.376 1.566-.663 2.258h10.244c-.287-.692-.502-1.49-.663-2.258C12.134 8.197 12 6.628 12 6a4.002 4.002 0 0 0-3.203-3.92L8 1.917zM14.22 12c.223.447.481.801.78 1H1c.299-.199.557-.553.78-1C2.68 10.2 3 6.88 3 6c0-2.42 1.72-4.44 4.005-4.901a1 1 0 1 1 1.99 0A5.002 5.002 0 0 1 13 6c0 .88.32 4.2 1.22 6z"/>
            </svg>
            <h3>Ready to monitor.</h3>
            <p>Select a template from the left or create a new one to begin.</p>
          </div>
        `}
      </div>
    `;
  }
}

customElements.define('price-alert-app', PriceAlertApp);