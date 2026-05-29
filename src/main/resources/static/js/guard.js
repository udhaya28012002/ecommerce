/**
 * Page guard - Protects pages based on authentication and role
 * Run this on page load to enforce access control
 * 
 * Usage in HTML:
 * <script src="/js/guard.js"></script>
 * <script>
 *   guard(); // Standard protection - authenticated users only
 *   // OR
 *   guard('admin'); // Admin-only protection
 * </script>
 */

function guard(requiredRole = null) {
  const isAuth = isAuthenticated();
  const userRole = getRole();
  const currentPage = window.location.pathname;

  // If not authenticated, redirect to login
  if (!isAuth) {
    // Don't redirect if already on login page
    if (!currentPage.includes('index.html') && currentPage !== '/') {
      window.location.href = '/index.html';
    }
    return;
  }

  // If page requires admin role but user is not admin
  if (requiredRole === 'admin' && userRole !== 'ROLE_ADMIN') {
    window.location.href = '/products.html';
    return;
  }

  // If page requires customer role but user is admin
  if (requiredRole === 'customer' && userRole === 'ROLE_ADMIN') {
    window.location.href = '/admin-dashboard.html';
    return;
  }
}

/**
 * Helper: Show/hide UI elements based on user role
 * Usage: <div class="admin-only">Admin content</div>
 * Then call: showRoleBasedElements()
 */
function showRoleBasedElements() {
  const userRole = getRole();
  
  // Hide all role-based elements first
  document.querySelectorAll('[data-role-required]').forEach(el => {
    el.style.display = 'none';
  });

  // Show elements matching user role
  if (userRole) {
    document.querySelectorAll(`[data-role-required="${userRole}"]`).forEach(el => {
      el.style.display = 'block';
    });
  }
}

/**
 * Protect an async function with authentication check
 * Useful for button click handlers
 * Usage: protectedAction(myAsyncFunction)
 */
async function protectedAction(asyncFunc) {
  if (!isAuthenticated()) {
    alert('Please login first');
    window.location.href = '/index.html';
    return;
  }

  try {
    return await asyncFunc();
  } catch (error) {
    //console.error('Action failed:', error);
    throw error;
  }
}

// Auto-run guard on page load if this script is included
document.addEventListener('DOMContentLoaded', function() {
  // Check if there's a data attribute specifying required role
  const pageRequiredRole = document.documentElement.getAttribute('data-required-role');
  if (pageRequiredRole) {
    guard(pageRequiredRole);
  }
});
