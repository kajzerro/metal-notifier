import { LitElement, html, css } from 'lit';
import './template-list.js';
import './template-detail.js';
import apiService from '../services/api-service.js';


export class AppMain extends LitElement {
  static properties = {
    selectedTemplate: { type: Object },
    templates: { type: Array }
  };

  static styles = css`
    :host {
      display: block;
      font-family: Arial, sans-serif;
    }
    .container {
      display: flex;
      height: 100vh;
    }
    .list-panel {
      width: 30%;
      border-right: 1px solid #ccc;
      padding: 1rem;
      overflow-y: auto;
    }
    .detail-panel {
      width: 70%;
      padding: 1rem;
      overflow-y: auto;
    }
  `;

  constructor() {
    super();
    this.templates = [];
    this.selectedTemplate = null;
    this.loadTemplates();
  }

  async loadTemplates() {
    try {
      this.templates = await apiService.getAllTemplates();
    } catch (error) {
      console.error('Error loading templates:', error);
    }
  }

  handleTemplateSelect(e) {
    this.selectedTemplate = e.detail;
  }

  handleTemplateCreated(e) {
    this.templates = [...this.templates, e.detail];
    this.selectedTemplate = e.detail;
  }
  handleTemplateUpdated(e) {
    const updatedTemplate = e.detail;
    console.log("got data")
    this.templates = this.templates.map(template =>
        template.id === updatedTemplate.id ? updatedTemplate : template
    );
    if (this.selectedTemplate?.id === updatedTemplate.id) {
      this.selectedTemplate = updatedTemplate;
    }
  }

  handleTemplateDeleted(e) {
    const deletedId = e.detail;
    this.templates = this.templates.filter(template => template.id !== deletedId);
    this.selectedTemplate = null;
  }

  render() {
    return html`
      <div class="container">
        <div class="list-panel">
          <template-list 
            .templates=${this.templates}
            @template-selected=${this.handleTemplateSelect}
            @template-created=${this.handleTemplateCreated}
          ></template-list>
        </div>
        <div class="detail-panel">
          ${this.selectedTemplate 
            ? html`<template-detail 
                .template=${this.selectedTemplate}
                @template-updated=${this.handleTemplateUpdated}
                @template-deleted=${this.handleTemplateDeleted}
              ></template-detail>` 
            : html`<p>Wybierz szablon z listy lub utwórz nowy.</p>`}
        </div>
      </div>
    `;
  }
}

customElements.define('app-main', AppMain);