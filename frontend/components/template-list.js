import { LitElement, html, css } from 'lit';
import apiService from "../services/api-service";

export class TemplateList extends LitElement {
  static properties = {
    templates: { type: Array }
  };

  static styles = css`
    :host {
      display: block;
    }
    .template-item {
      padding: 0.5rem;
      margin-bottom: 0.5rem;
      border: 1px solid #ddd;
      cursor: pointer;
      background-color: #f9f9f9;
    }
    .template-item:hover {
      background-color: #f0f0f0;
    }
    .selected {
      background-color: #e0e0e0;
    }
    button {
      margin-bottom: 1rem;
      padding: 0.5rem 1rem;
      background-color: #4CAF50;
      color: white;
      border: none;
      cursor: pointer;
    }
  `;

  constructor() {
    super();
    this.templates = [];
  }

  selectTemplate(template) {
    this.dispatchEvent(new CustomEvent('template-selected', {
      detail: template
    }));
  }

  async createNewTemplate() {
    const newTemplate = {
      title: 'Nowy szablon',
      content: '',
      recipients: [],
      rules: []
    };

    try {
      const savedTemplate = await apiService.createTemplate(newTemplate);
      this.dispatchEvent(new CustomEvent('template-created', {
        detail: savedTemplate
      }));
    } catch (error) {
      console.error('Error creating template:', error);
    }
  }

  render() {
    return html`
      <button @click=${this.createNewTemplate}>Dodaj nowy</button>
      <div>
        ${this.templates.map(template => html`
          <div class="template-item" @click=${() => this.selectTemplate(template)}>
            ${template.title}
          </div>
        `)}
      </div>
    `;
  }
}

customElements.define('template-list', TemplateList);
