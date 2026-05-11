// api-service.js
const API_BASE_URL = import.meta.env.VITE_API_URL || 'http://localhost:8080/api';
const TEMPLATES_URL = `${API_BASE_URL}/templates`;

export const TemplateService = {
  getAll: async (page = 0, size = 10, search = '') => {
    const url = `${TEMPLATES_URL}?page=${page}&size=${size}&search=${encodeURIComponent(search)}`;
    const response = await fetch(url);
    if (!response.ok) throw new Error('Network response was not ok');
    return response.json();
  },

  save: async (template) => {
    const response = await fetch(TEMPLATES_URL, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(template)
    });
    if (!response.ok) throw new Error('Save failed');
    return await response.json();
  },

  update: async (id, template) => {
    console.log(`API_SERVICE: Updating record ${id} at: ${TEMPLATES_URL}/${id}`);
    const response = await fetch(`${TEMPLATES_URL}/${id}`, {
      method: 'PUT',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(template)
    });
    
    if (!response.ok) {
      const errorText = await response.text();
      throw new Error(errorText || 'Update failed');
    }
    return await response.json();
  },

  getSupportedItems: async () => {
    const response = await fetch(`${TEMPLATES_URL}/supported-items`);
    if (!response.ok) throw new Error('Failed to fetch items');
    return response.json();
  }
};