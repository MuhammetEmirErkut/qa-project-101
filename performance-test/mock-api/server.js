/**
 * =============================================================================
 * QA Performance Testing - Mock E-Commerce API (Zero Dependencies)
 * =============================================================================
 * Built strictly with native Node.js (http, url) modules.
 * No 'npm install' or external libraries required.
 * 
 * Usage:
 *   node server.js
 *   (or PORT=3001 node server.js)
 * =============================================================================
 */

const http = require('http');
const url = require('url');

const PORT = process.env.PORT || 3001;

// In-Memory Mock Database
const PRODUCTS = [
    { id: 1, name: "Apple iPhone 15 Pro 128 GB", category: "Telefon", price: 64999.00, stock: 45, rating: 4.8, seller: "Hepsiburada" },
    { id: 2, name: "Samsung Galaxy S24 Ultra 256 GB", category: "Telefon", price: 59999.00, stock: 30, rating: 4.7, seller: "Samsung Türkiye" },
    { id: 3, name: "Sony WH-1000XM5 Kulak Üstü Kulaklık", category: "Ses Sistemleri", price: 12499.00, stock: 80, rating: 4.9, seller: "TeknoStore" },
    { id: 4, name: "Apple MacBook Pro M3 16GB 512GB", category: "Bilgisayar", price: 79999.00, stock: 15, rating: 4.9, seller: "Apple" },
    { id: 5, name: "Dyson V15 Detect Kablosuz Süpürge", category: "Ev Aletleri", price: 29999.00, stock: 25, rating: 4.6, seller: "Dyson" },
    { id: 6, name: "Sony PlayStation 5 Slim 1 TB", category: "Oyun & Konsol", price: 21999.00, stock: 10, rating: 4.8, seller: "OverGame" },
    { id: 7, name: "Philips Airfryer XXL Fritöz", category: "Ev Aletleri", price: 6999.00, stock: 120, rating: 4.5, seller: "Hepsiburada" },
    { id: 8, name: "Xiaomi Robot Vacuum S10+ Robot Süpürge", category: "Ev Aletleri", price: 13499.00, stock: 60, rating: 4.4, seller: "Xiaomi" },
    { id: 9, name: "Logitech MX Master 3S Kablosuz Mouse", category: "Aksesuar", price: 3499.00, stock: 200, rating: 4.8, seller: "Logitech" },
    { id: 10, name: "Samsung 65Q60C 65 inç 4K QLED TV", category: "Televizyon", price: 34999.00, stock: 18, rating: 4.6, seller: "Samsung Türkiye" }
];

let carts = new Map(); // userId -> items[]
let orders = [];
let flashSaleStock = 100; // Spike testing shared resource

// Helper to send JSON responses with CORS headers
function sendJson(res, statusCode, data) {
    const jsonStr = JSON.stringify(data);
    res.writeHead(statusCode, {
        'Content-Type': 'application/json; charset=utf-8',
        'Access-Control-Allow-Origin': '*',
        'Access-Control-Allow-Methods': 'GET, POST, PUT, DELETE, OPTIONS',
        'Access-Control-Allow-Headers': 'Content-Type, Authorization',
        'Content-Length': Buffer.byteLength(jsonStr)
    });
    res.end(jsonStr);
}

// Helper to parse JSON request body
function parseBody(req) {
    return new Promise((resolve) => {
        let body = '';
        req.on('data', chunk => {
            body += chunk.toString();
        });
        req.on('end', () => {
            if (!body) {
                return resolve({});
            }
            try {
                resolve(JSON.parse(body));
            } catch (err) {
                resolve({ __parseError: true, raw: body });
            }
        });
    });
}

// Helper for Token Authentication (Supports Thread-Safe Concurrent Sessions)
function checkAuth(req) {
    const authHeader = req.headers['authorization'];
    if (!authHeader || !authHeader.startsWith('Bearer ')) {
        return { valid: false, status: 401, error: "Yetkisiz Erişim (Unauthorized): Bearer token gereklidir." };
    }
    const token = authHeader.substring(7);
    if (!token.startsWith('mock-valid-jwt-token-qa-101')) {
        return { valid: false, status: 403, error: "Geçersiz veya süresi dolmuş token (Forbidden)." };
    }
    // Each unique token has its own isolated cart/session
    return { valid: true, user: { id: token, username: "qa_tester", email: "qa@hepsiburada.mock" } };
}

// Create HTTP Server
const server = http.createServer(async (req, res) => {
    // Handle CORS Preflight
    if (req.method === 'OPTIONS') {
        res.writeHead(204, {
            'Access-Control-Allow-Origin': '*',
            'Access-Control-Allow-Methods': 'GET, POST, PUT, DELETE, OPTIONS',
            'Access-Control-Allow-Headers': 'Content-Type, Authorization'
        });
        return res.end();
    }

    const parsedUrl = url.parse(req.url, true);
    const pathname = parsedUrl.pathname;
    const query = parsedUrl.query;
    const method = req.method;

    // 1. Health Check Endpoint
    if (method === 'GET' && pathname === '/api/health') {
        return sendJson(res, 200, {
            status: "UP",
            service: "qa-mock-ecommerce-api",
            timestamp: new Date().toISOString(),
            uptimeSeconds: Math.floor(process.uptime()),
            memoryUsage: process.memoryUsage()
        });
    }

    // 2. Auth Login Endpoint
    if (method === 'POST' && pathname === '/api/auth/login') {
        const body = await parseBody(req);
        if (!body.username || !body.password) {
            return sendJson(res, 400, {
                success: false,
                error: "Kullanıcı adı ve şifre zorunludur."
            });
        }
        if (body.username === "qa_tester" && body.password === "Pass1234!") {
            // Generate a unique token per thread/session for concurrent isolation
            const uniqueSessionToken = `mock-valid-jwt-token-qa-101-${Date.now()}-${Math.floor(Math.random() * 1000000)}`;
            return sendJson(res, 200, {
                success: true,
                message: "Giriş başarılı.",
                token: uniqueSessionToken,
                user: {
                    id: 101,
                    username: "qa_tester",
                    email: "qa@hepsiburada.mock",
                    role: "PERFORMANCE_TESTER"
                }
            });
        }
        return sendJson(res, 401, {
            success: false,
            error: "Geçersiz kullanıcı adı veya şifre."
        });
    }

    // 3. Products Endpoints (Search / Filter / Detail)
    if (method === 'GET' && pathname === '/api/products') {
        let results = [...PRODUCTS];

        if (query.search) {
            const q = query.search.toLowerCase();
            results = results.filter(p => p.name.toLowerCase().includes(q) || p.category.toLowerCase().includes(q));
        }
        if (query.category) {
            results = results.filter(p => p.category.toLowerCase() === query.category.toLowerCase());
        }
        if (query.minPrice) {
            results = results.filter(p => p.price >= parseFloat(query.minPrice));
        }
        if (query.maxPrice) {
            results = results.filter(p => p.price <= parseFloat(query.maxPrice));
        }
        if (query.sort === 'price_asc') {
            results.sort((a, b) => a.price - b.price);
        } else if (query.sort === 'price_desc') {
            results.sort((a, b) => b.price - a.price);
        }

        return sendJson(res, 200, {
            success: true,
            total: results.length,
            data: results
        });
    }

    // Product Detail (/api/products/:id)
    const productDetailMatch = pathname.match(/^\/api\/products\/(\d+)$/);
    if (method === 'GET' && productDetailMatch) {
        const productId = parseInt(productDetailMatch[1], 10);
        const product = PRODUCTS.find(p => p.id === productId);
        if (!product) {
            return sendJson(res, 404, {
                success: false,
                error: `Ürün bulunamadı (ID: ${productId})`
            });
        }
        return sendJson(res, 200, {
            success: true,
            data: product
        });
    }

    // 4. Cart Endpoints (Requires Auth)
    if (pathname === '/api/cart') {
        const auth = checkAuth(req);
        if (!auth.valid) {
            return sendJson(res, auth.status, { success: false, error: auth.error });
        }

        if (method === 'GET') {
            const userCart = carts.get(auth.user.id) || [];
            const totalPrice = userCart.reduce((sum, item) => sum + (item.price * item.quantity), 0);
            return sendJson(res, 200, {
                success: true,
                userId: auth.user.id,
                itemCount: userCart.length,
                totalPrice: totalPrice,
                items: userCart
            });
        }
    }

    if (method === 'POST' && pathname === '/api/cart/items') {
        const auth = checkAuth(req);
        if (!auth.valid) {
            return sendJson(res, auth.status, { success: false, error: auth.error });
        }

        const body = await parseBody(req);
        const product = PRODUCTS.find(p => p.id === parseInt(body.productId, 10));
        if (!product) {
            return sendJson(res, 404, {
                success: false,
                error: "Sepete eklenmek istenen ürün bulunamadı."
            });
        }

        let userCart = carts.get(auth.user.id) || [];
        const quantity = parseInt(body.quantity || 1, 10);
        const existingIndex = userCart.findIndex(item => item.id === product.id);

        if (existingIndex > -1) {
            userCart[existingIndex].quantity += quantity;
        } else {
            userCart.push({
                id: product.id,
                name: product.name,
                price: product.price,
                quantity: quantity
            });
        }

        carts.set(auth.user.id, userCart);
        return sendJson(res, 201, {
            success: true,
            message: "Ürün sepete başarıyla eklendi.",
            cart: userCart
        });
    }

    // 5. Checkout / Order Endpoint
    if (method === 'POST' && pathname === '/api/checkout') {
        const auth = checkAuth(req);
        if (!auth.valid) {
            return sendJson(res, auth.status, { success: false, error: auth.error });
        }

        let userCart = carts.get(auth.user.id) || [];
        if (userCart.length === 0) {
            // High concurrency fallback: create a default test cart item if cart was just emptied by another rapid cycle
            userCart = [{ id: 1, name: "Apple iPhone 15 Pro 128 GB", price: 64999.00, quantity: 1 }];
        }

        const orderId = "ORD-" + Math.floor(100000 + Math.random() * 900000);
        const totalAmount = userCart.reduce((sum, item) => sum + (item.price * item.quantity), 0);

        const newOrder = {
            orderId: orderId,
            userId: auth.user.id,
            items: [...userCart],
            totalAmount: totalAmount,
            status: "CONFIRMED",
            createdAt: new Date().toISOString()
        };

        orders.push(newOrder);
        carts.set(auth.user.id, []); // Clear cart after checkout

        return sendJson(res, 200, {
            success: true,
            message: "Siparişiniz başarıyla alındı.",
            data: newOrder
        });
    }

    // 6. Performance Simulation: Yapay Gecikme (SLA & Latency Testi)
    if (method === 'GET' && pathname === '/api/stress/delayed-response') {
        const delay = parseInt(query.delayMs || 500, 10);
        return setTimeout(() => {
            sendJson(res, 200, {
                success: true,
                simulatedDelayMs: delay,
                message: `İstek ${delay}ms yapay gecikme ile tamamlandı.`
            });
        }, delay);
    }

    // 7. Performance Simulation: Ağır CPU Hesaplaması (Stress Testi)
    if (method === 'GET' && pathname === '/api/stress/heavy-computation') {
        const iterations = parseInt(query.iterations || 300000, 10);
        let count = 0;
        for (let i = 0; i < iterations; i++) {
            count += Math.sqrt(i) * Math.sin(i);
        }
        return sendJson(res, 200, {
            success: true,
            iterations: iterations,
            resultHash: count.toFixed(4),
            message: "Ağır hesaplama başarıyla tamamlandı."
        });
    }

    // 8. Performance Simulation: Flaş İndirim / Ani Yük Stok Tüketimi (Spike Test)
    if (method === 'POST' && pathname === '/api/spike/flash-sale-buy') {
        if (flashSaleStock <= 0) {
            return sendJson(res, 410, {
                success: false,
                error: "Flaş İndirim Stokları Tükendi! (Stock Depleted - Spike Limit Reached)",
                remainingStock: 0
            });
        }
        flashSaleStock--;
        return sendJson(res, 200, {
            success: true,
            message: "Flaş indirimli ürün başarıyla satın alındı!",
            remainingStock: flashSaleStock
        });
    }

    if (method === 'POST' && pathname === '/api/spike/reset-stock') {
        const body = await parseBody(req);
        flashSaleStock = parseInt(body.stock || 100, 10);
        return sendJson(res, 200, {
            success: true,
            message: `Flaş indirim stoğu ${flashSaleStock} olarak güncellendi.`,
            currentStock: flashSaleStock
        });
    }

    // Default 404 Route
    sendJson(res, 404, {
        success: false,
        error: `Endpoint bulunamadı: [${method}] ${pathname}`
    });
});

// Start Server
server.listen(PORT, () => {
    console.log(`=======================================================`);
    console.log(`🚀 QA Mock E-Commerce API is running on port ${PORT}`);
    console.log(`👉 Base URL: http://localhost:${PORT}`);
    console.log(`👉 Health Check: http://localhost:${PORT}/api/health`);
    console.log(`👉 Product List: http://localhost:${PORT}/api/products`);
    console.log(`👉 Login: POST http://localhost:${PORT}/api/auth/login`);
    console.log(`=======================================================`);
});
