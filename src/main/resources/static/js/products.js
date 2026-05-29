(function() {
  let page = 0;
  const size = 10;

  const tableBody = document.querySelector('#products-table tbody');
  const messages = document.getElementById('messages');
  const pageNum = document.getElementById('page-num');

  const controls = {
    searchName: document.getElementById('search-name'),
    minPrice: document.getElementById('min-price'),
    maxPrice: document.getElementById('max-price'),
    inStock: document.getElementById('in-stock-only'),
    categoryId: document.getElementById('filter-category')
  };

  document.getElementById('apply-filters').addEventListener('click', () => {
    page = 0;
    loadProducts();
  });

  document.getElementById('clear-filters').addEventListener('click', () => {
    controls.searchName.value = '';
    controls.minPrice.value = '';
    controls.maxPrice.value = '';
    controls.inStock.checked = false;
    controls.categoryId.value = '';
    page = 0;
    loadProducts();
  });

  document.getElementById('prev-page').addEventListener('click', () => {
    if (page > 0) {
      page -= 1;
      loadProducts();
    }
  });

  document.getElementById('next-page').addEventListener('click', () => {
    page += 1;
    loadProducts();
  });

  document.getElementById('sort-price-asc').addEventListener('click', () => {
    sortByPrice(true);
  });
  document.getElementById('sort-price-desc').addEventListener('click', () => {
    sortByPrice(false);
  });
  document.getElementById('sort-name-asc').addEventListener('click', () => {
    sortByName(true);
  });
  document.getElementById('sort-name-desc').addEventListener('click', () => {
    sortByName(false);
  });

  controls.searchName.addEventListener('keyup', function(e) {
    if (e.key === 'Enter') {
      page = 0;
      loadProducts();
    }
  });

  async function showMessage(text, type = 'error') {
    messages.textContent = text;
    messages.className = type === 'error' ? 'error-message' : 'success-message';
    setTimeout(() => { messages.textContent = ''; messages.className = ''; }, 5000);
  }

  function extractProducts(resp) {
    if (!resp) return [];
    if (Array.isArray(resp)) return resp;
    if (resp.content && Array.isArray(resp.content)) return resp.content;
    // Some endpoints might return an object with data
    if (resp.products && Array.isArray(resp.products)) return resp.products;
    return [];
  }

  async function loadProducts() {
    try {
      pageNum.textContent = (page + 1);
      // Determine which filter to apply
      const name = controls.searchName.value.trim();
      const min = controls.minPrice.value;
      const max = controls.maxPrice.value;
      const inStock = controls.inStock.checked;
      const categoryId = controls.categoryId.value.trim();

      let resp;

      if (inStock) {
        resp = await request('GET', `/api/products/getInStockProducts?page=${page}&size=${size}`);
      } else if (name) {
        resp = await request('GET', `/api/products/findByMatchingName/${encodeURIComponent(name)}?page=${page}&size=${size}`);
      } else if (categoryId) {
        resp = await request('GET', `/api/products/listProductByCategory/${encodeURIComponent(categoryId)}?page=${page}&size=${size}`);
      } else if (min || max) {
        const qmin = min ? min : 0;
        const qmax = max ? max : 999999999;
        resp = await request('GET', `/api/products/filterProducts?minPrice=${qmin}&maxPrice=${qmax}&page=${page}&size=${size}`);
      } else {
        resp = await request('GET', `/api/products/listAllProducts?page=${page}&size=${size}`);
      }

      const products = extractProducts(resp);
      renderTable(products);
    } catch (err) {
      //console.error('Load products failed', err);
      showMessage(err.message || 'Failed to load products', 'error');
    }
  }

  function renderTable(products) {
    tableBody.innerHTML = '';
    if (!products || products.length === 0) {
      const tr = document.createElement('tr');
      tr.innerHTML = `<td colspan="5">No products found</td>`;
      tableBody.appendChild(tr);
      return;
    }

    products.forEach(p => {
      const tr = document.createElement('tr');
      const name = p.name || p.productName || '';
      const price = typeof p.price !== 'undefined' ? p.price : (p.sellingPrice || '');
      const desc = p.shortDescription || p.description || '';
      const stock = p.stock || (p.inventory && p.inventory.productQuantity) || '';
      tr.innerHTML = `
        <td>${escapeHtml(name)}</td>
        <td>${escapeHtml(price)}</td>
        <td>${escapeHtml(desc)}</td>
        <td>${escapeHtml(stock)}</td>
        <td>
          <button class="btn view-btn" data-id="${p.productId}">View</button>
          <button class="btn addcart-btn" data-id="${p.productId}">Add to Cart</button>
        </td>
      `;
      tableBody.appendChild(tr);
    });

    // Wire buttons
    document.querySelectorAll('.view-btn').forEach(b => {
      b.addEventListener('click', (e) => {
        const id = e.currentTarget.getAttribute('data-id');
        window.location.href = `/product-detail.html?id=${id}`;
      });
    });

    document.querySelectorAll('.addcart-btn').forEach(b => {
      b.addEventListener('click', async (e) => {
        const id = e.currentTarget.getAttribute('data-id');
        try {
          await request('POST', '/api/cart/addToCart', { productId: Number(id), quantity: 1 });
          showMessage('Added to cart', 'success');
        } catch (err) {
          //console.error('Add to cart failed', err);
          showMessage(err.message || 'Failed to add to cart', 'error');
        }
      });
    });
  }

  function escapeHtml(str) {
    if (str === null || str === undefined) return '';
    return String(str).replace(/[&<>"']/g, function (s) {
      return ({'&':'&amp;','<':'&lt;','>':'&gt;','"':'&quot;',"'":'&#39;'})[s];
    });
  }

  async function sortByPrice(asc) {
    try {
      const resp = await request('GET', `/api/products/sortByPrice/${asc}?page=${page}&size=${size}`);
      const products = extractProducts(resp);
      renderTable(products);
    } catch (err) {
      //console.error('Sort failed', err);
      showMessage('Sort failed', 'error');
    }
  }

  async function sortByName(asc) {
    try {
      const resp = await request('GET', `/api/products/sortByName/${asc}?page=${page}&size=${size}`);
      const products = extractProducts(resp);
      renderTable(products);
    } catch (err) {
      //console.error('Sort failed', err);
      showMessage('Sort failed', 'error');
    }
  }

  // initial load
  document.addEventListener('DOMContentLoaded', () => {
    loadProducts();
  });

  controls.inStock.addEventListener('change', () => { page = 0; loadProducts(); });

})();
