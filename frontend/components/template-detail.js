import { LitElement, html, css } from 'lit';
import './rule-form.js';
import './recipient-form.js';
import apiService from "../services/api-service";

export class TemplateDetail extends LitElement {
  static properties = {
    template: { type: Object },
    editMode: { type: Boolean }
  };

  static styles = css`
    :host {
      display: block;
    }
    .form-group {
      margin-bottom: 1rem;
    }
    label {
      display: block;
      margin-bottom: 0.5rem;
      font-weight: bold;
    }
    input, textarea {
      width: 100%;
      padding: 0.5rem;
      border: 1px solid #ddd;
    }
    textarea {
      min-height: 100px;
    }
    .button-group {
      display: flex;
      gap: 1rem;
      margin-top: 1rem;
    }
    button {
      padding: 0.5rem 1rem;
      background-color: #4CAF50;
      color: white;
      border: none;
      cursor: pointer;
    }
    button.cancel {
      background-color: #f44336;
    }
    button.delete {
      background-color: #f44336;
    }
    .section {
      margin-top: 2rem;
      border-top: 1px solid #ddd;
      padding-top: 1rem;
    }
    h2 {
      margin-top: 0;
    }
  `;

  constructor() {
    super();
    this.template = null;
    this.editMode = false;
    this.editData = {};
  }

  willUpdate(changedProperties) {
    if (changedProperties.has('template') && this.template) {
      this.editData = { ...this.template };
    }
  }

  startEditing() {
    this.editMode = true;
    this.editData = { ...this.template };
  }

  cancelEditing() {
    this.editMode = false;
  }

  handleInputChange(e, field) {
    this.editData = {
      ...this.editData,
      [field]: e.target.value
    };
  }

  async saveTemplate() {
    try {
      const updatedTemplate = await apiService.updateTemplate(this.template.id, this.editData);
      this.template = updatedTemplate;
      this.editMode = false;

      this.dispatchEvent(new CustomEvent('template-updated', {
        detail: updatedTemplate
      }));
    } catch (error) {
      console.error('Error updating template:', error);
    }
  }

  async deleteTemplate() {
    if (!confirm('Czy na pewno chcesz usunąć ten szablon?')) {
      return;
    }

    try {
      await apiService.deleteTemplate(this.template.id);
      this.dispatchEvent(new CustomEvent('template-deleted', {
        detail: this.template.id
      }));
    } catch (error) {
      console.error('Error deleting template:', error);
    }
  }

  handleTemplateUpdated(e) {
    this.template = e.detail;
    this.requestUpdate();

    this.dispatchEvent(new CustomEvent('template-updated', {
      detail: e.detail
    }));
  }


  render() {
    if (!this.template) {
      return html`<p>Nie wybrano szablonu.</p>`;
    }

    return html`
    <div>
      ${this.editMode ? this.renderEditForm() : this.renderViewMode()}

      <div class="section">
        <h2>Odbiorcy</h2>
        <recipient-form
            .template=${this.template}
            @template-updated=${this.handleTemplateUpdated}
        ></recipient-form>

        <ul>
          ${this.template.recipients.map(recipient => html`
              <li>${recipient.email} <button @click=${() => this.removeRecipient(recipient.id)}>Usuń</button></li>
            `)}
        </ul>
      </div>
      <div class="section">
        <h2>Reguły wysyłki</h2>
        <p>Wszystkie poniższe reguły muszą być spełnione, aby powiadomienie zostało wysłane.</p>
        <rule-form 
          .template=${this.template}
          @template-updated=${this.handleTemplateUpdated}
        ></rule-form>
        <div class="rules-list">
          ${this.template.rules && this.template.rules.length > 0 ?
        html`
              <h3>Aktualnie zdefiniowane reguły:</h3>
              <ul>
                ${this.template.rules.map(rule => this.renderRule(rule))}
              </ul>
            ` :
        html`<p>Brak zdefiniowanych reguł. Dodaj przynajmniej jedną regułę powyżej.</p>`
    }
        </div>
      </div>
    </div>
  `;
  }

  getOperatorDisplayName(operatorId) {
    const operatorMap = {
      'ITEM_IS': 'Metal to',
      'ITEM_IS_NOT': 'Metal to nie',
      'PRICE_IS_EQUAL_TO': 'Cena jest równa',
      'PRICE_IS_GREATER_THAN': 'Cena jest większa niż',
      'PRICE_IS_GREATER_THAN_OR_EQUAL_TO': 'Cena jest większa lub równa',
      'PRICE_IS_LESS_THAN': 'Cena jest mniejsza niż',
      'PRICE_IS_LESS_THAN_OR_EQUAL_TO': 'Cena jest mniejsza lub równa'
    };

    return operatorMap[operatorId] || operatorId;
  }

  renderRule(rule) {
    const operatorDisplay = this.getOperatorDisplayName(rule.operator);

    return html`
    <li>
      ${operatorDisplay} ${rule.operand}
      <button @click=${() => this.removeRule(rule.id)}>Usuń</button>
    </li>
  `;
  }

  renderViewMode() {
    return html`
      <h1>${this.template.title}</h1>
      <p><strong>Treść:</strong> ${this.template.content}</p>
      <div class="button-group">
        <button @click=${this.startEditing}>Edytuj</button>
        <button class="delete" @click=${this.deleteTemplate}>Usuń</button>
      </div>
    `;
  }

  renderEditForm() {
    return html`
      <div class="form-group">
        <label for="title">Tytuł</label>
        <input 
          type="text" 
          id="title" 
          .value=${this.editData.title || ''} 
          @input=${e => this.handleInputChange(e, 'title')}
        >
      </div>
      <div class="form-group">
        <label for="content">Treść</label>
        <textarea 
          id="content" 
          .value=${this.editData.content || ''} 
          @input=${e => this.handleInputChange(e, 'content')}
        ></textarea>
      </div>
      <div class="button-group">
        <button @click=${this.saveTemplate}>Zapisz</button>
        <button class="cancel" @click=${this.cancelEditing}>Anuluj</button>
      </div>
    `;
  }

  async removeRecipient(id) {
    const updatedTemplate = {
      ...this.template,
      recipients: this.template.recipients.filter(recipient => recipient.id !== id)
    };
    
    try {
      const savedTemplate = await apiService.updateTemplate(this.template.id, updatedTemplate);
      this.template = savedTemplate;
      this.dispatchEvent(new CustomEvent('template-updated', {
        detail: savedTemplate
      }));
    } catch (error) {
      console.error('Error removing recipient:', error);
    }
  }

  async removeRule(id) {
    const updatedTemplate = {
      ...this.template,
      rules: this.template.rules.filter(rule => rule.id !== id)
    };

    try {
      const savedTemplate = await apiService.updateTemplate(this.template.id, updatedTemplate);
      this.template = savedTemplate;

      this.dispatchEvent(new CustomEvent('template-updated', {
        detail: savedTemplate
      }));
    } catch (error) {
      console.error('Error removing rule:', error);
    }
  }
}

customElements.define('template-detail', TemplateDetail);