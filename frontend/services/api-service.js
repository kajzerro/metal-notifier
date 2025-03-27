
class ApiService {
  constructor(baseUrl = 'http://localhost:8080/api') {
    this.baseUrl = baseUrl;
  }

  async fetchWithErrorHandling(url, options = {}) {
    try {
      const response = await fetch(url, options);

      if (!response.ok) {
        throw new Error(`HTTP error! Status: ${response.status}`);
      }

      const contentType = response.headers.get('content-type');
      if (contentType && contentType.includes('application/json')) {
        return await response.json();
      } else if (response.status !== 204) {
        return await response.text();
      }

      return null;
    } catch (error) {
      console.error(`API error: ${error.message}`);
      throw error;
    }
  }

  async getAllTemplates() {
    return this.fetchWithErrorHandling(`${this.baseUrl}/templates`);
  }

  async createTemplate(template) {
    return this.fetchWithErrorHandling(`${this.baseUrl}/templates`, {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
      },
      body: JSON.stringify(template),
    });
  }

  // async updateTemplate(id, template) {
  //   return this.fetchWithErrorHandling(`${this.baseUrl}/templates/${id}`, {
  //     method: 'PUT',
  //     headers: {
  //       'Content-Type': 'application/json',
  //     },
  //     body: JSON.stringify(template),
  //   });
  // }

  async updateTemplate(templateId, data) {
    const response = await fetch(`${this.baseUrl}/templates/${templateId}`, {
      method: 'PUT',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(data),
    });
    if (!response.ok) {
      throw new Error(`Error updating template: ${response.statusText}`);
    }
    return response.json();
  }

  async deleteTemplate(id) {
    return this.fetchWithErrorHandling(`${this.baseUrl}/templates/${id}`, {
      method: 'DELETE',
    });
  }

}

export default new ApiService();