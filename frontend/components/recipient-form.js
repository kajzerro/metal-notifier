import { LitElement, html, css } from 'lit';

export class RecipientForm extends LitElement {
  static properties = {
    template: { type: Object },
    emailValue: { type: String }
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
    }
    input {
      padding: 0.3rem;
      border: 1px solid #ddd;
      flex-grow: 1;
    }
    button {
      padding: 0.3rem 0.7rem;
      background-color: #4CAF50;
      color: white;
      border: none;
      cursor: pointer;
    }
  `;

  constructor() {
    super();
    this.template = null;
    this.emailValue = '';
  }

  handleEmailChange(e) {
    this.emailValue = e.target.value;
  }

  async addRecipient() {
    if (!this.validateEmail(this.emailValue)) {
      alert('Proszę podać poprawny adres email.');
      return;
    }

    const newRecipient = {
      email: this.emailValue
    };

    try {
      console.log('Adding new recipient:', newRecipient);

      const updatedTemplate = {
        ...this.template,
        recipients: [...(this.template.recipients || []), newRecipient]
      };

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

      this.dispatchEvent(new CustomEvent('template-updated', {
        detail: savedTemplate
      }));

      this.emailValue = '';
    } catch (error) {
      console.error('Error adding recipient:', error);
      alert('Wystąpił błąd podczas dodawania odbiorcy: ' + error.message);
    }
  }

  validateEmail(email) {
    const re = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
    return re.test(email);
  }

  render() {
    return html`
      <div class="form-group">
        <input 
          type="email" 
          placeholder="Adres email" 
          .value=${this.emailValue} 
          @input=${this.handleEmailChange}
          @keypress=${e => e.key === 'Enter' && this.addRecipient()}
        >
        <button @click=${this.addRecipient}>Dodaj</button>
      </div>
    `;
  }
}

customElements.define('recipient-form', RecipientForm);