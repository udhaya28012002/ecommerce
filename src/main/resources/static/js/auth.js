/**
 * Login user with username and password
 * Stores tokens and role in localStorage
 * Redirects based on user role
 * @param {string} username
 * @param {string} password
 */
async function login(username, password) {
  try {
    const response = await fetch(API_BASE + '/api/authenticate', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ username, password, loginMethod: 'STANDARD' })
    });

    const data = await response.json();

    if (!response.ok) {
      throw new Error(data.message || 'Login failed');
    }

    if (data.accessToken && data.refreshToken) {
        localStorage.setItem('accessToken', data.accessToken);
        localStorage.setItem('refreshToken', data.refreshToken);

        // Decode JWT to get role (assuming standard JWT structure)
        const decodedToken = decodeJWT(data.accessToken);
        //const role = decodedToken.authorities ? decodedToken : 'ROLE_CUSTOMER';

        const role = (decodedToken || 'ROLE_CUSTOMER').toString().replace(/[\[\]]/g, '').trim();

        localStorage.setItem('role', role);

        console.log('Login successful. Role:', role);
        console.log(role === 'ROLE_ADMIN')

        // Redirect based on role
        if (role === 'ROLE_ADMIN') {
            window.location.href = '/admin-dashboard.html';
        } else {
            window.location.href = '/products.html';
        }
    } else {
        throw new Error('Invalid authentication response');
    }
  } catch (error) {
    //console.error('Login failed:', error);
    alert('Login failed: ' + error.message);
  }
}

/**
 * Register new user
 * Stores tokens and role in localStorage
 * Redirects to products page (customer role)
 * @param {string} name
 * @param {string} userName
 * @param {string} emailId
 * @param {string} password
 * @param {string} confirmPassword
 * @param {string} contactNo
 * @param {object} address - { street, city, state, pincode }
 */
async function register(name, userName, emailId, password, confirmPassword, contactNo, address) {
  try {
    const response = await request('POST', '/api/createUser', {
      name,
      userName,
      emailId,
      password,
      confirmPassword,
      contactNo,
      address
    });

    if (response.accessToken && response.refreshToken) {
      localStorage.setItem('accessToken', response.accessToken);
      localStorage.setItem('refreshToken', response.refreshToken);
      localStorage.setItem('role', 'ROLE_CUSTOMER');

      window.location.href = '/products.html';
    } else {
      throw new Error('Invalid registration response');
    }
  } catch (error) {
    //console.error('Registration failed:', error);
    alert('Registration failed: ' + error.message);
  }
}

/**
 * Logout user
 * Calls refresh token revocation endpoint
 * Clears localStorage and redirects to login
 */
async function logout() {
  try {
    const refreshToken = localStorage.getItem('refreshToken');

    if (refreshToken) {
      await request('POST', '/api/deleteRefreshAuth', {
        refreshToken
      });
    }

    localStorage.clear();
    window.location.href = '/index.html';
  } catch (error) {
    //console.error('Logout error:', error);
    // Clear anyway even if request fails
    localStorage.clear();
    window.location.href = '/index.html';
  }
}

/**
 * Check if user is authenticated
 * @returns {boolean} true if accessToken exists
 */
function isAuthenticated() {
  return localStorage.getItem('accessToken') !== null;
}

/**
 * Get user role from localStorage
 * @returns {string|null} role (ROLE_ADMIN or ROLE_CUSTOMER) or null
 */
function getRole() {
  return localStorage.getItem('role');
}

/**
 * Decode JWT token to extract claims
 * Basic JWT decoder (no verification - for client-side use only)
 * @param {string} token - JWT token
 * @returns {object} Decoded payload
 */
function decodeJWT(token) {
  try {
    const parts = token.split('.');
    if (parts.length !== 3) {
      throw new Error('Invalid token format');
    }

    const payload = parts[1];
    const decoded = JSON.parse(atob(payload));

    console.log('Decoded JWT:', decoded.role);

    return decoded.role;
  } catch (error) {
    //console.error('Failed to decode JWT:', error);
    return {};
  }
}

/**
 * Get current username from JWT token
 * @returns {string|null} username or null
 */
function getCurrentUsername() {
  const token = localStorage.getItem('accessToken');
  if (!token) return null;

  const decoded = decodeJWT(token);
  return decoded.sub || null; // 'sub' is typically the username in JWT
}
