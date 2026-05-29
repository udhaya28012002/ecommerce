(function() {
  const messages = document.getElementById('messages');

  function showMessage(text, type = 'error') {
    messages.textContent = text;
    messages.className = type === 'error' ? 'error-message' : 'success-message';
    setTimeout(() => { messages.textContent = ''; messages.className = ''; }, 6000);
  }

  async function loadProfile() {
    try {
      const resp = await request('GET', '/api/users/getUser');
      renderProfile(resp);
    } catch (err) {
      //console.error('Failed to load profile', err);
      showMessage(err.message || 'Failed to load profile', 'error');
    }
  }

  function renderProfile(user) {
    const container = document.getElementById('account-fields');
    container.innerHTML = '';
    if (!user) return;

    const fields = [
      ['Name', user.name || user.fullName || ''],
      ['Username', user.userName || user.username || ''],
      ['Email', user.emailId || user.email || ''],
      ['Contact', user.contactNo || ''],
      ['Status', user.status || '']
    ];

    fields.forEach(f => {
      const row = document.createElement('div');
      row.style.marginBottom = '8px';
      row.innerHTML = `<strong>${escapeHtml(f[0])}:</strong> <span>${escapeHtml(f[1])}</span>`;
      container.appendChild(row);
    });

    // Addresses
    const addrTitle = document.createElement('h4');
    addrTitle.textContent = 'Addresses';
    container.appendChild(addrTitle);

    const addrs = user.address || user.addresses || [];
    if (!addrs || addrs.length === 0) {
      const p = document.createElement('div');
      p.textContent = 'No addresses';
      container.appendChild(p);
    } else {
      addrs.forEach(a => {
        const d = document.createElement('div');
        d.style.marginBottom = '6px';
        d.textContent = `${a.street || ''}, ${a.city || ''}, ${a.state || ''} - ${a.pincode || ''}`;
        container.appendChild(d);
      });
    }
  }

  function escapeHtml(str) {
    if (str === null || str === undefined) return '';
    return String(str).replace(/[&<>"']/g, function (s) {
      return ({'&':'&amp;','<':'&lt;','>':'&gt;','"':'&quot;',"'":'&#39;'})[s];
    });
  }

  // Change password
  document.getElementById('password-form').addEventListener('submit', async (e) => {
    e.preventDefault();
    const oldPassword = document.getElementById('old-password').value;
    const newPassword = document.getElementById('new-password').value;
    const confirm = document.getElementById('confirm-password').value;

    try {
      await request('PATCH', '/api/users/password', { oldPassword, newPassword, confirmPassword: confirm });
      showMessage('Password changed', 'success');
      document.getElementById('password-form').reset();
    } catch (err) {
      //console.error('Change password failed', err);
      showMessage(err.message || 'Failed to change password', 'error');
    }
  });

  // Update contact
  document.getElementById('update-contact').addEventListener('click', async () => {
    const val = document.getElementById('contact-input').value.trim();
    if (!val) return showMessage('Enter contact number', 'error');
    try {
      await request('PATCH', '/api/users/contactNo', { changeField: val });
      showMessage('Contact updated', 'success');
      await loadProfile();
    } catch (err) {
      //console.error('Update contact failed', err);
      showMessage(err.message || 'Failed to update contact', 'error');
    }
  });

  // Add address
  document.getElementById('address-form').addEventListener('submit', async (e) => {
    e.preventDefault();
    const street = document.getElementById('addr-street').value.trim();
    const city = document.getElementById('addr-city').value.trim();
    const state = document.getElementById('addr-state').value.trim();
    const pincode = document.getElementById('addr-pincode').value.trim();

    try {
      await request('PATCH', '/api/users/address', { street, city, state, pincode });
      showMessage('Address added', 'success');
      document.getElementById('address-form').reset();
      await loadProfile();
    } catch (err) {
      //console.error('Add address failed', err);
      showMessage(err.message || 'Failed to add address', 'error');
    }
  });

  // Delete account
  document.getElementById('delete-account').addEventListener('click', async () => {
    if (!confirm('Delete your account? This cannot be undone.')) return;
    try {
      await request('PATCH', '/api/users/deleteUser');
      // clear and redirect to index
      localStorage.clear();
      window.location.href = '/index.html';
    } catch (err) {
      //console.error('Delete account failed', err);
      showMessage(err.message || 'Failed to delete account', 'error');
    }
  });

  document.addEventListener('DOMContentLoaded', loadProfile);

})();
