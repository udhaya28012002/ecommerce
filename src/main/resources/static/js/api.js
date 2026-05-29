const API_BASE = 'http://localhost:9096';

/**
 * Makes an HTTP request with JWT authentication
 * Automatically handles token refresh on 401
 * @param {string} method - HTTP method (GET, POST, PATCH, DELETE, etc.)
 * @param {string} path - API endpoint path (e.g., '/api/products/listAllProducts')
 * @param {object} body - Request body (optional, for POST/PATCH/PUT)
 * @returns {Promise<any>} Response data
 */
async function request(method, path, body = null) {
  let accessToken = localStorage.getItem('accessToken');
  
  const options = {
    method,
    headers: {
      'Content-Type': 'application/json'
    }
  };

  // Attach Authorization header if token exists
  if (accessToken) {
    options.headers['Authorization'] = `Bearer ${accessToken}`;
  }

  // Add request body if provided
  if (body) {
    options.body = JSON.stringify(body);
  }

  try {
    let response = await fetch(API_BASE + path, options);

    // Handle 401 - Token expired, try refresh
    if (response.status === 401) {
      const refreshToken = localStorage.getItem('refreshToken');

      if (!refreshToken) {
        // No refresh token, redirect to login
        localStorage.clear();
        window.location.href = '/index.html';
        return;
      }

      try {
        // Attempt to refresh access token
        const refreshResponse = await fetch(API_BASE + '/api/refreshAuth', {
          method: 'POST',
          headers: { 'Content-Type': 'application/json' },
          body: JSON.stringify({ refreshToken })
        });

        if (refreshResponse.ok) {
          const newAccessToken = await refreshResponse.text();
          localStorage.setItem('accessToken', newAccessToken);

          // Retry original request with new token
          options.headers['Authorization'] = `Bearer ${newAccessToken}`;
          response = await fetch(API_BASE + path, options);
        } else {
          // Refresh failed - logout
          localStorage.clear();
          window.location.href = '/index.html';
          return;
        }
      } catch (error) {
        // Refresh error - logout
        localStorage.clear();
        window.location.href = '/index.html';
        return;
      }
    }

    // Parse and return response
    const contentType = response.headers.get('content-type');
    let data;

    if (contentType && contentType.includes('application/json')) {
      data = await response.json();
    } else {
      data = await response.text();
    }

    //console.log("API Response:", { path, status: response.status, data });
    if (!response.ok) {
      throw new Error(data || `HTTP ${response.status}`);
    }

    return data;
  } catch (error) {
    //console.error('Request failed:', error);
    throw error;
  }
}

// Export for use in modules
if (typeof module !== 'undefined' && module.exports) {
  module.exports = { request, API_BASE };
}
