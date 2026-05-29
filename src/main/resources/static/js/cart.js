(function() {
  const tableBody = document.querySelector('#cart-table tbody');
  const messages = document.getElementById('messages');
  const orderResult = document.getElementById('order-result');

  async function showMessage(text, type = 'error') {
    messages.textContent = text;
    messages.className = type === 'error' ? 'error-message' : 'success-message';
    setTimeout(() => { messages.textContent = ''; messages.className = ''; }, 6000);
  }

  function showOrder(text, success = true) {
    orderResult.textContent = text;
    orderResult.className = success ? 'success-message' : 'error-message';
  }

  function flattenItems(cartResp) {
    // cartResp.cartItemsCategoryResponseDtoList -> categories -> cartItemsResponseDtoList
    if (!cartResp) return [];
    const categories = cartResp.cartItemsCategoryResponseDtoList || [];
    const items = [];
    categories.forEach(cat => {
      const list = cat.cartItemsResponseDtoList || [];
      list.forEach(i => items.push(i));
    });
    return items;
  }

  async function loadCart() {
    tableBody.innerHTML = '';
    messages.textContent = '';
    messages.className = '';
    try {
      const resp = await request('GET', '/api/cart/getCart');
      const items = flattenItems(resp);
      renderTable(items);
      renderSummary(resp);
    } catch (err) {
      //console.error('Failed to load cart', err);
      showMessage(err.message || 'Failed to load cart', 'error');
    }
  }

  function renderTable(items) {
    tableBody.innerHTML = '';
    if (!items || items.length === 0) {
      const tr = document.createElement('tr');
      tr.innerHTML = `<td colspan="5">Your cart is empty</td>`;
      tableBody.appendChild(tr);
      return;
    }

    items.forEach(item => {
      const tr = document.createElement('tr');
      const name = item.productName || item.name || '';
      const quantity = item.quantity || 0;
      const price = item.price || item.sellingPrice || 0;
      const subtotal = item.subtotal || (quantity * price);

      tr.innerHTML = `
        <td>${escapeHtml(name)}</td>
        <td>
          <button class="btn dec" data-id="${item.productId}">-</button>
          <span style="margin:0 8px">${quantity}</span>
          <button class="btn inc" data-id="${item.productId}">+</button>
        </td>
        <td>${escapeHtml(price)}</td>
        <td>${escapeHtml(subtotal)}</td>
        <td>
          <button class="btn remove" data-id="${item.productId}">Remove</button>
        </td>
      `;
      tableBody.appendChild(tr);
    });

    // Wire buttons
    document.querySelectorAll('.inc').forEach(b => b.addEventListener('click', async (e) => {
      const id = e.currentTarget.getAttribute('data-id');
      try {
        await request('PATCH', `/api/cart/update?productId=${id}&quantity=1&positive=true`);
        await loadCart();
      } catch (err) {
        //console.error('Increment failed', err);
        showMessage(err.message || 'Failed to update cart', 'error');
      }
    }));

    document.querySelectorAll('.dec').forEach(b => b.addEventListener('click', async (e) => {
      const id = e.currentTarget.getAttribute('data-id');
      try {
        await request('PATCH', `/api/cart/update?productId=${id}&quantity=1&positive=false`);
        await loadCart();
      } catch (err) {
        //console.error('Decrement failed', err);
        showMessage(err.message || 'Failed to update cart', 'error');
      }
    }));

    document.querySelectorAll('.remove').forEach(b => b.addEventListener('click', async (e) => {
      const id = e.currentTarget.getAttribute('data-id');
      try {
        await request('DELETE', `/api/cart/delete/${id}`);
        await loadCart();
      } catch (err) {
        //console.error('Remove failed', err);
        showMessage(err.message || 'Failed to remove item', 'error');
      }
    }));
  }

  function renderSummary(cartResp) {
    const total = document.getElementById('summary-total');
    const discount = document.getElementById('summary-discount');
    const delivery = document.getElementById('summary-delivery');
    const final = document.getElementById('summary-final');

    if (!cartResp) {
      total.textContent = '-';
      discount.textContent = '-';
      delivery.textContent = '-';
      final.textContent = '-';
      return;
    }

    total.textContent = cartResp.totalPrice ?? '-';
    discount.textContent = cartResp.discount ?? '-';
    delivery.textContent = cartResp.deliveryCharge ?? '-';
    final.textContent = cartResp.finalPrice ?? '-';
  }

  function escapeHtml(str) {
    if (str === null || str === undefined) return '';
    return String(str).replace(/[&<>"']/g, function (s) {
      return ({'&':'&amp;','<':'&lt;','>':'&gt;','"':'&quot;',"'":'&#39;'})[s];
    });
  }

  document.getElementById('clear-all').addEventListener('click', async () => {
    try {
      await request('DELETE', '/api/cart/deleteAll');
      await loadCart();
      showMessage('Cart cleared', 'success');
    } catch (err) {
      //console.error('Clear cart failed', err);
      showMessage(err.message || 'Failed to clear cart', 'error');
    }
  });

  document.getElementById('place-order').addEventListener('click', async () => {
    try {
      // Get current cart items
      const resp = await request('GET', '/api/cart/getCart');
      const items = flattenForOrder(resp);
      if (!items || items.length === 0) {
        showOrder('Cart is empty', false);
        return;
      }

      const couponCode = document.getElementById('coupon-code').value.trim();
      const body = { couponCode: couponCode || null, items };

      const orderResp = await request('POST', '/api/orders/placeOrder', body);
      // Expect OrdersResDto with orderNumber
      const orderNumber = (orderResp && (orderResp.orderNumber || orderResp.orderId || orderResp.id)) || null;
      if (orderNumber) {
        showOrder('Order placed: ' + orderNumber, true);
        // Clear cart view
        await loadCart();
      } else {
        showOrder('Order placed', true);
        await loadCart();
      }
    } catch (err) {
      //console.error('Place order failed', err);
      showOrder(err.message || 'Failed to place order', false);
    }
  });

  function flattenForOrder(cartResp) {
    const items = flattenItems(cartResp);
    return items.map(i => ({ productId: i.productId, quantity: i.quantity }));
  }

  // helper duplicate from above
  function flattenItems(cartResp) {
    if (!cartResp) return [];
    const categories = cartResp.cartItemsCategoryResponseDtoList || [];
    const items = [];
    categories.forEach(cat => {
      const list = cat.cartItemsResponseDtoList || [];
      list.forEach(i => items.push(i));
    });
    return items;
  }

  // initial load
  document.addEventListener('DOMContentLoaded', () => {
    loadCart();
  });

})();
