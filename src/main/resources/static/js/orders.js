(function() {
  const tableBody = document.querySelector('#orders-table tbody');
  const messages = document.getElementById('messages');

  function showMessage(text, type = 'error') {
    messages.textContent = text;
    messages.className = type === 'error' ? 'error-message' : 'success-message';
    setTimeout(() => { messages.textContent = ''; messages.className = ''; }, 6000);
  }

  async function loadOrders() {
    try {
      const resp = await request('GET', '/api/orders/getOrders');
      const orders = Array.isArray(resp) ? resp : (resp.orders || resp.content || []);
      renderTable(orders);
    } catch (err) {
      //console.error('Failed to load orders', err);
      showMessage(err.message || 'Failed to load orders', 'error');
    }
  }

  function renderTable(orders) {
    tableBody.innerHTML = '';
    if (!orders || orders.length === 0) {
      const tr = document.createElement('tr');
      tr.innerHTML = `<td colspan="5">No orders found</td>`;
      tableBody.appendChild(tr);
      return;
    }

    orders.forEach(o => {
      const tr = document.createElement('tr');
      const orderNumber = o.orderNumber || o.orderId || o.id;
      const date = o.orderDate || o.orderDateTime || '';
      const status = o.orderStatus || o.status || '';
      const finalPrice = o.finalPrice || o.total || '';

      tr.innerHTML = `
        <td>${escapeHtml(orderNumber)}</td>
        <td>${escapeHtml(date)}</td>
        <td>${escapeHtml(status)}</td>
        <td>${escapeHtml(finalPrice)}</td>
        <td>
          <button class="btn view-order" data-id="${orderNumber}">View</button>
          <button class="btn cancel-order" data-id="${orderNumber}">Cancel</button>
        </td>
      `;
      tableBody.appendChild(tr);
    });

    // Wire buttons
    document.querySelectorAll('.view-order').forEach(b => b.addEventListener('click', async (e) => {
      const id = e.currentTarget.getAttribute('data-id');
      await toggleDetails(e.currentTarget, id);
    }));

    document.querySelectorAll('.cancel-order').forEach(b => b.addEventListener('click', async (e) => {
      const id = e.currentTarget.getAttribute('data-id');
      try {
        // Confirm
        if (!confirm(`Cancel order ${id}?`)) return;
        await request('PATCH', `/api/orders/cancelOrder/${encodeURIComponent(id)}`);
        showMessage('Order cancelled', 'success');
        await loadOrders();
      } catch (err) {
        //console.error('Cancel failed', err);
        showMessage(err.message || 'Failed to cancel order', 'error');
      }
    }));

    // Hide cancel buttons for orders that are not cancellable
    document.querySelectorAll('.cancel-order').forEach(btn => {
      const tr = btn.closest('tr');
      const status = tr.querySelector('td:nth-child(3)').textContent.trim().toUpperCase();
      if (['DELIVERED','CANCELLED','SHIPPED','OUT_OF_DELIVERY'].includes(status)) {
        btn.style.display = 'none';
      }
    });
  }

  async function toggleDetails(buttonEl, orderNumber) {
    const row = buttonEl.closest('tr');
    const next = row.nextElementSibling;
    // If already expanded, collapse
    if (next && next.classList.contains('order-details-row')) {
      next.remove();
      return;
    }

    try {
      const resp = await request('GET', `/api/orders/getOrder/${encodeURIComponent(orderNumber)}`);
      const items = resp.orderItemsResponse || resp.orderItems || resp.orderItemsResponseDto || [];

      const detailsTr = document.createElement('tr');
      detailsTr.className = 'order-details-row';
      const td = document.createElement('td');
      td.colSpan = 5;

      let html = '<table style="width:100%;"><thead><tr><th>ProductId</th><th>Quantity</th><th>Price</th></tr></thead><tbody>';
      items.forEach(it => {
        const pid = it.productId || (it.products && it.products.productId) || '';
        const qty = it.quantity || '';
        const price = it.sellingPrice || it.price || it.totalPrice || '';
        html += `<tr><td>${escapeHtml(pid)}</td><td>${escapeHtml(qty)}</td><td>${escapeHtml(price)}</td></tr>`;
      });
      html += '</tbody></table>';

      td.innerHTML = html;
      detailsTr.appendChild(td);
      row.parentNode.insertBefore(detailsTr, row.nextSibling);
    } catch (err) {
      //console.error('Fetch order details failed', err);
      showMessage(err.message || 'Failed to fetch order details', 'error');
    }
  }

  function escapeHtml(str) {
    if (str === null || str === undefined) return '';
    return String(str).replace(/[&<>"']/g, function (s) {
      return ({'&':'&amp;','<':'&lt;','>':'&gt;','"':'&quot;',"'":'&#39;'})[s];
    });
  }

  document.getElementById('fetch-order-btn').addEventListener('click', async () => {
    const id = document.getElementById('fetch-order-number').value.trim();
    if (!id) return showMessage('Enter order number', 'error');
    try {
      const resp = await request('GET', `/api/orders/getOrder/${encodeURIComponent(id)}`);
      // Show single order as table with one row
      renderTable([resp]);
    } catch (err) {
      //console.error('Fetch single order failed', err);
      showMessage(err.message || 'Failed to fetch order', 'error');
    }
  });

  document.getElementById('refresh-orders').addEventListener('click', loadOrders);

  // initial load
  loadOrders();
})();
