import {css, html, LitElement} from 'lit';

const ITEM_IS = 'Item is';
const ITEM_IS_NOT = 'Item is not';
const PRICE_IS_EQUAL_TO = 'Price is equal to';
const PRICE_IS_GREATER_THAN = 'Price is greater than';
const PRICE_IS_GREATER_THAN_OR_EQUAL = 'Price is greater than or equal';
const PRICE_IS_LESS_THAN = 'Price is less than';
const PRICE_IS_LESS_THAN_OR_EQUAL = 'Price is less than or equal';

export class RuleForm extends LitElement {
  static properties = {
    template: { type: Object },
    operatorValue: { type: String },
    operandValue: { type: String },
    metalType: { type: String },
    formVisible: { type: Boolean }
  };

  static styles = css`
    :host {
      display: block;
      margin-bottom: 1rem;
    }
    .form-group {
      margin-bottom: 0.5rem;
      display: flex;
      align-items: center;
      gap: 0.5rem;
      flex-wrap: wrap;
    }
    select, input {
      padding: 0.3rem;
      border: 1px solid #ddd;
    }
    button {
      padding: 0.3rem 0.7rem;
      background-color: #4CAF50;
      color: white;
      border: none;
      cursor: pointer;
    }
    button.secondary {
      background-color: #607D8B;
    }
    .rule-preview {
      margin-top: 0.5rem;
      padding: 0.5rem;
      background-color: #f5f5f5;
      border: 1px solid #ddd;
      border-radius: 3px;
    }
    .rules-info {
      margin-bottom: 1rem;
      padding: 0.5rem;
      background-color: #e8f4f8;
      border-left: 3px solid #2196F3;
      font-size: 0.9rem;
    }
    label {
      font-weight: bold;
      margin-right: 0.5rem;
    }
  `;

  constructor() {
    super();
    this.template = null;
    this.operatorValue = 'ITEM_IS';
    this.operandValue = '';
    this.metalType = 'gold';
    this.formVisible = false;
  }

  toggleForm() {
    this.formVisible = !this.formVisible;
  }

  handleOperatorChange(e) {
    this.operatorValue = e.target.value;
  }

  handleOperandChange(e) {
    this.operandValue = e.target.value;
  }

  handleMetalTypeChange(e) {
    this.metalType = e.target.value;
  }

  async addRule() {
    const isItemOperator = this.operatorValue === 'ITEM_IS' || this.operatorValue === 'ITEM_IS_NOT';
    const operand = isItemOperator ? this.metalType : this.operandValue;

    if (!isItemOperator && (this.operandValue === '' || isNaN(parseFloat(this.operandValue)))) {
      alert('Proszę podać poprawną wartość liczbową dla ceny.');
      return;
    }

    const newRule = {
      operator: this.operatorValue,
      operand: operand
    };

    try {
      console.log('Dodawanie reguły:', newRule);

      const updatedTemplate = {
        ...this.template,
        rules: [...(this.template.rules || []), newRule]
      };

      console.log('Aktualizacja szablonu:', updatedTemplate);

      const response = await fetch(`http://localhost:8080/api/templates/${this.template.id}`, {
        method: 'PUT',
        headers: {
          'Content-Type': 'application/json'
        },
        body: JSON.stringify(updatedTemplate)
      });

      if (!response.ok) {
        throw new Error(`HTTP error! Status: ${response.status}`);
      }

      const savedTemplate = await response.json();
      console.log('Otrzymana odpowiedź z serwera:', savedTemplate);

      this.dispatchEvent(new CustomEvent('template-updated', {
        detail: savedTemplate
      }));

      this.operandValue = '';
      this.formVisible = false;
    } catch (error) {
      console.error('Error adding rule:', error);
      alert('Wystąpił błąd podczas dodawania reguły: ' + error.message);
    }
  }

  getOperatorDisplayName(operatorId) {
    const operatorMap = {
              'ITEM_IS': 'Item is',
              'ITEM_IS_NOT': ITEM_IS_NOT,
              'PRICE_IS_EQUAL_TO': PRICE_IS_EQUAL_TO,
              'PRICE_IS_GREATER_THAN': PRICE_IS_GREATER_THAN,
              'PRICE_IS_GREATER_THAN_OR_EQUAL_TO': PRICE_IS_GREATER_THAN_OR_EQUAL,
              'PRICE_IS_LESS_THAN': PRICE_IS_LESS_THAN,
              'PRICE_IS_LESS_THAN_OR_EQUAL_TO': PRICE_IS_LESS_THAN_OR_EQUAL
  };

    return operatorMap[operatorId] || operatorId;
  }

  render() {
    const isItemOperator = this.operatorValue === 'ITEM_IS' || this.operatorValue === 'ITEM_IS_NOT';

    return html`
      <div class="rules-info">
        <strong>Zasada działania reguł:</strong> Wszystkie zdefiniowane reguły muszą być spełnione jednocześnie (koniunkcja),
        aby powiadomienie zostało wysłane dla danego sygnału cenowego.
      </div>
      
      ${this.formVisible ?
        html`
          <div class="form-container">
            <div class="form-group">
              <label>Operator:</label>
              <select @change=${this.handleOperatorChange}>
                <option value="ITEM_IS">${ITEM_IS}</option>
                <option value="ITEM_IS_NOT">${ITEM_IS_NOT}</option>
                <option value="PRICE_IS_EQUAL_TO">${PRICE_IS_EQUAL_TO}</option>
                <option value="PRICE_IS_GREATER_THAN">${PRICE_IS_GREATER_THAN}</option>
                <option value="PRICE_IS_GREATER_THAN_OR_EQUAL_TO">${PRICE_IS_GREATER_THAN_OR_EQUAL}</option>
                <option value="PRICE_IS_LESS_THAN">${PRICE_IS_LESS_THAN}</option>
                <option value="PRICE_IS_LESS_THAN_OR_EQUAL_TO">${PRICE_IS_LESS_THAN_OR_EQUAL}</option>
              </select>
            </div>
            
            <div class="form-group">
              <label>Wartość:</label>
              ${isItemOperator
            ? html`
                  <select @change=${this.handleMetalTypeChange}>
                    <option value="gold">gold</option>
                    <option value="silver">silver</option>
                    <option value="platinum">platinum</option>
                  </select>
                `
            : html`
                  <input 
                    type="number" 
                    step="0.01" 
                    min="0"
                    placeholder="Wartość ceny" 
                    .value=${this.operandValue} 
                    @input=${this.handleOperandChange}
                  >
                `
        }
            </div>
            
            <div class="rule-preview">
              <strong>Podgląd reguły:</strong> ${this.getOperatorDisplayName(this.operatorValue)} 
              ${isItemOperator ? this.metalType : this.operandValue || '(podaj wartość)'}
            </div>
            
            <div class="form-group" style="margin-top: 1rem;">
              <button @click=${this.addRule}>Dodaj regułę</button>
              <button class="secondary" @click=${this.toggleForm}>Anuluj</button>
            </div>
          </div>
        `
        : html`
          <button @click=${this.toggleForm}>+ Dodaj nową regułę</button>
        `
    }
    `;
  }
}

customElements.define('rule-form', RuleForm);