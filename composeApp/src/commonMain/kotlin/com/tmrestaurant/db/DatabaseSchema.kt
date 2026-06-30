package com.tmrestaurant.db

object DatabaseSchema {

    const val VERSION = 1

    const val SYNC_COLS = "_sync_status TEXT NOT NULL DEFAULT 'pending', _updated_at INTEGER NOT NULL, _server_id TEXT"
    const val SYNC_COLS_NO_DEFAULT = "_sync_status TEXT NOT NULL, _updated_at INTEGER NOT NULL, _server_id TEXT"

    // ─── SETTINGS ───────────────────────────────────────────────────────────

    val COMPANY_SETTINGS = """
        CREATE TABLE IF NOT EXISTS company_settings (
            id INTEGER PRIMARY KEY DEFAULT 1 CHECK (id = 1),
            business_name TEXT NOT NULL DEFAULT 'CRIS POLLO',
            rnc TEXT NOT NULL DEFAULT '123-456789-0',
            phone TEXT NOT NULL DEFAULT '809-555-1234',
            email TEXT NOT NULL DEFAULT 'info@tm-restaurante.com',
            currency TEXT NOT NULL DEFAULT 'RD$ - Peso Dominicano',
            address TEXT NOT NULL DEFAULT 'Av. Principal #123, Santo Domingo',
            tax_percent TEXT NOT NULL DEFAULT '18',
            suggested_tip_percent TEXT NOT NULL DEFAULT '10',
            logo_path TEXT,
            $SYNC_COLS
        )
    """.trimIndent()

    val VISUAL_SETTINGS = """
        CREATE TABLE IF NOT EXISTS visual_settings (
            id INTEGER PRIMARY KEY DEFAULT 1 CHECK (id = 1),
            theme_mode TEXT NOT NULL DEFAULT 'light',
            primary_color TEXT NOT NULL DEFAULT '#8758F2',
            $SYNC_COLS
        )
    """.trimIndent()

    val SALES_SETTINGS = """
        CREATE TABLE IF NOT EXISTS sales_settings (
            id INTEGER PRIMARY KEY DEFAULT 1 CHECK (id = 1),
            tax_mode TEXT NOT NULL DEFAULT 'included',
            tax_percent TEXT NOT NULL DEFAULT '18',
            allow_discounts INTEGER NOT NULL DEFAULT 0,
            require_customer INTEGER NOT NULL DEFAULT 0,
            allow_out_of_stock_sales INTEGER NOT NULL DEFAULT 0,
            auto_send_to_kitchen INTEGER NOT NULL DEFAULT 1,
            $SYNC_COLS
        )
    """.trimIndent()

    val PAYMENT_METHODS = """
        CREATE TABLE IF NOT EXISTS payment_methods (
            id TEXT PRIMARY KEY,
            name TEXT NOT NULL,
            percentage TEXT NOT NULL DEFAULT '0',
            enabled INTEGER NOT NULL DEFAULT 1,
            $SYNC_COLS
        )
    """.trimIndent()

    val PRINT_SETTINGS = """
        CREATE TABLE IF NOT EXISTS print_settings (
            id INTEGER PRIMARY KEY DEFAULT 1 CHECK (id = 1),
            selected_printer TEXT NOT NULL DEFAULT 'POS80 Printer',
            search_type TEXT NOT NULL DEFAULT 'USB',
            paper_width_mm TEXT NOT NULL DEFAULT '80',
            copies INTEGER NOT NULL DEFAULT 2,
            text_size TEXT NOT NULL DEFAULT 'large',
            margin_left TEXT NOT NULL DEFAULT '2',
            margin_right TEXT NOT NULL DEFAULT '2',
            margin_top TEXT NOT NULL DEFAULT '2',
            margin_bottom TEXT NOT NULL DEFAULT '2',
            useful_width_mm TEXT NOT NULL DEFAULT '70',
            logo_width_mm TEXT NOT NULL DEFAULT '51',
            logo_height_mm TEXT NOT NULL DEFAULT '20',
            show_company_logo INTEGER NOT NULL DEFAULT 1,
            show_company_name INTEGER NOT NULL DEFAULT 1,
            show_company_rnc INTEGER NOT NULL DEFAULT 1,
            show_company_address INTEGER NOT NULL DEFAULT 1,
            show_company_phone INTEGER NOT NULL DEFAULT 1,
            show_company_email INTEGER NOT NULL DEFAULT 0,
            show_customer_name INTEGER NOT NULL DEFAULT 1,
            show_customer_rnc INTEGER NOT NULL DEFAULT 1,
            show_customer_phone INTEGER NOT NULL DEFAULT 0,
            show_customer_email INTEGER NOT NULL DEFAULT 0,
            show_date_time INTEGER NOT NULL DEFAULT 1,
            show_cashier_name INTEGER NOT NULL DEFAULT 1,
            show_taxes INTEGER NOT NULL DEFAULT 1,
            show_discounts INTEGER NOT NULL DEFAULT 1,
            show_tip INTEGER NOT NULL DEFAULT 1,
            show_payment_method INTEGER NOT NULL DEFAULT 1,
            show_cash_change INTEGER NOT NULL DEFAULT 1,
            show_thank_you_message INTEGER NOT NULL DEFAULT 1,
            show_qr INTEGER NOT NULL DEFAULT 1,
            show_receipt_number INTEGER NOT NULL DEFAULT 1,
            show_ncf INTEGER NOT NULL DEFAULT 1,
            show_ncf_expiry INTEGER NOT NULL DEFAULT 1,
            show_invoice_title INTEGER NOT NULL DEFAULT 1,
            show_items INTEGER NOT NULL DEFAULT 1,
            show_subtotal INTEGER NOT NULL DEFAULT 1,
            show_total INTEGER NOT NULL DEFAULT 1,
            show_item_count INTEGER NOT NULL DEFAULT 1,
            show_cash_register INTEGER NOT NULL DEFAULT 1,
            show_note INTEGER NOT NULL DEFAULT 1,
            show_tax_summary INTEGER NOT NULL DEFAULT 1,
            show_return_policy INTEGER NOT NULL DEFAULT 1,
            show_barcode INTEGER NOT NULL DEFAULT 1,
            show_footer_date INTEGER NOT NULL DEFAULT 1,
            thank_you_message TEXT NOT NULL DEFAULT 'Gracias por su compra!',
            $SYNC_COLS
        )
    """.trimIndent()

    val NOTIFICATION_SETTINGS = """
        CREATE TABLE IF NOT EXISTS notification_settings (
            id INTEGER PRIMARY KEY DEFAULT 1 CHECK (id = 1),
            enabled INTEGER NOT NULL DEFAULT 0,
            smtp_server TEXT NOT NULL DEFAULT 'smtp.gmail.com',
            smtp_port TEXT NOT NULL DEFAULT '465',
            sender_email TEXT NOT NULL DEFAULT 'tmposrd@gmail.com',
            app_password TEXT NOT NULL DEFAULT '',
            sender_name TEXT NOT NULL DEFAULT 'TM-RESTAURANTE',
            destination_emails TEXT NOT NULL DEFAULT 'wilsontomas1986@gmail.com',
            ssl_tls INTEGER NOT NULL DEFAULT 1,
            send_on_logout INTEGER NOT NULL DEFAULT 0,
            send_on_cash_close INTEGER NOT NULL DEFAULT 1,
            send_on_cancel_invoice INTEGER NOT NULL DEFAULT 1,
            send_on_delete_invoice INTEGER NOT NULL DEFAULT 1,
            $SYNC_COLS
        )
    """.trimIndent()

    val LICENSE_SETTINGS = """
        CREATE TABLE IF NOT EXISTS license_settings (
            id INTEGER PRIMARY KEY DEFAULT 1 CHECK (id = 1),
            status TEXT NOT NULL DEFAULT 'Prueba (7 dias)',
            company_name TEXT NOT NULL DEFAULT 'TM-RESTAURANTE',
            last_check TEXT NOT NULL DEFAULT '',
            next_check TEXT NOT NULL DEFAULT '',
            offline_days_remaining TEXT NOT NULL DEFAULT '5 de 5',
            device_code TEXT NOT NULL DEFAULT '',
            check_interval_minutes TEXT NOT NULL DEFAULT '360',
            $SYNC_COLS
        )
    """.trimIndent()

    val SERVER_SETTINGS = """
        CREATE TABLE IF NOT EXISTS server_settings (
            id INTEGER PRIMARY KEY DEFAULT 1 CHECK (id = 1),
            enabled INTEGER NOT NULL DEFAULT 1,
            server_url TEXT NOT NULL DEFAULT '',
            api_route TEXT NOT NULL DEFAULT '/api2',
            api_key TEXT NOT NULL DEFAULT '',
            token_configured INTEGER NOT NULL DEFAULT 0,
            sync_invoices INTEGER NOT NULL DEFAULT 1,
            automatic_send INTEGER NOT NULL DEFAULT 1,
            sync_products INTEGER NOT NULL DEFAULT 0,
            sync_customers INTEGER NOT NULL DEFAULT 0,
            sync_cash_closings INTEGER NOT NULL DEFAULT 0,
            last_sync TEXT NOT NULL DEFAULT '',
            $SYNC_COLS
        )
    """.trimIndent()

    val SYSTEM_SETTINGS = """
        CREATE TABLE IF NOT EXISTS system_settings (
            id INTEGER PRIMARY KEY DEFAULT 1 CHECK (id = 1),
            local_server_ip TEXT NOT NULL DEFAULT '192.168.1.7',
            local_server_port TEXT NOT NULL DEFAULT '8787',
            detected_ips TEXT NOT NULL DEFAULT '',
            available_url TEXT NOT NULL DEFAULT '',
            $SYNC_COLS
        )
    """.trimIndent()

    val ADMIN_CARDS = """
        CREATE TABLE IF NOT EXISTS admin_cards (
            user_id TEXT NOT NULL,
            user_name TEXT NOT NULL,
            user_role TEXT NOT NULL,
            card_number TEXT NOT NULL,
            $SYNC_COLS,
            PRIMARY KEY (user_id, card_number)
        )
    """.trimIndent()

    // ─── FISCAL ─────────────────────────────────────────────────────────────

    val FISCAL_SEQUENCES = """
        CREATE TABLE IF NOT EXISTS fiscal_sequences (
            type TEXT PRIMARY KEY,
            enabled INTEGER NOT NULL DEFAULT 1,
            prefix TEXT NOT NULL,
            current INTEGER NOT NULL DEFAULT 39,
            range_start INTEGER NOT NULL DEFAULT 1,
            range_end INTEGER NOT NULL DEFAULT 99999999,
            valid_until TEXT NOT NULL DEFAULT '31/12/2027',
            $SYNC_COLS
        )
    """.trimIndent()

    // ─── PRODUCTS & CATEGORIES ──────────────────────────────────────────────

    val PRODUCTS = """
        CREATE TABLE IF NOT EXISTS products (
            id INTEGER PRIMARY KEY,
            name TEXT NOT NULL,
            code TEXT NOT NULL DEFAULT '',
            barcode TEXT NOT NULL DEFAULT '',
            category TEXT NOT NULL DEFAULT '',
            description TEXT NOT NULL DEFAULT '',
            price REAL NOT NULL DEFAULT 0.0,
            cost REAL NOT NULL DEFAULT 0.0,
            tax_percent REAL NOT NULL DEFAULT 18.0,
            stock INTEGER NOT NULL DEFAULT 0,
            stock_alert INTEGER NOT NULL DEFAULT 1,
            image_path TEXT,
            active INTEGER NOT NULL DEFAULT 1,
            sell_in_pos INTEGER NOT NULL DEFAULT 1,
            send_to_kitchen INTEGER NOT NULL DEFAULT 1,
            send_to_bar INTEGER NOT NULL DEFAULT 0,
            favorite INTEGER NOT NULL DEFAULT 0,
            control_inventory INTEGER NOT NULL DEFAULT 1,
            sell_by_weight INTEGER NOT NULL DEFAULT 0,
            modifier_group_ids TEXT NOT NULL DEFAULT '',
            uid TEXT NOT NULL,
            created_at INTEGER NOT NULL,
            updated_at INTEGER NOT NULL,
            $SYNC_COLS
        )
    """.trimIndent()

    val CATEGORIES = """
        CREATE TABLE IF NOT EXISTS categories (
            id INTEGER PRIMARY KEY,
            name TEXT NOT NULL,
            description TEXT NOT NULL DEFAULT '',
            color_type TEXT NOT NULL DEFAULT 'Gray',
            active INTEGER NOT NULL DEFAULT 1,
            visible_pos INTEGER NOT NULL DEFAULT 1,
            ord INTEGER NOT NULL DEFAULT 0,
            uid TEXT NOT NULL,
            created_at INTEGER NOT NULL,
            updated_at INTEGER NOT NULL,
            $SYNC_COLS
        )
    """.trimIndent()

    // ─── CLIENTES ────────────────────────────────────────────────────────────

    val CLIENTES = """
        CREATE TABLE IF NOT EXISTS clientes (
            id TEXT PRIMARY KEY,
            nombre TEXT NOT NULL DEFAULT '',
            rnc TEXT NOT NULL DEFAULT '',
            telefono TEXT NOT NULL DEFAULT '',
            email TEXT NOT NULL DEFAULT '',
            direccion TEXT NOT NULL DEFAULT '',
            tipo TEXT NOT NULL DEFAULT 'Consumidor Final',
            limite_credito REAL NOT NULL DEFAULT 0.0,
            loyalty_points INTEGER NOT NULL DEFAULT 0,
            total_spent REAL NOT NULL DEFAULT 0.0,
            uid TEXT NOT NULL,
            created_at INTEGER NOT NULL,
            updated_at INTEGER NOT NULL,
            $SYNC_COLS
        )
    """.trimIndent()

    // ─── USUARIOS ────────────────────────────────────────────────────────────

    val USUARIOS = """
        CREATE TABLE IF NOT EXISTS usuarios (
            id TEXT PRIMARY KEY,
            name TEXT NOT NULL DEFAULT '',
            pin TEXT NOT NULL DEFAULT '',
            password TEXT NOT NULL DEFAULT '',
            role TEXT NOT NULL DEFAULT 'CAJERO',
            uid TEXT NOT NULL,
            created_at INTEGER NOT NULL,
            updated_at INTEGER NOT NULL,
            failed_attempts INTEGER NOT NULL DEFAULT 0,
            locked_until INTEGER,
            must_change_credentials INTEGER NOT NULL DEFAULT 0,
            pin_hash TEXT NOT NULL DEFAULT '',
            password_hash TEXT NOT NULL DEFAULT '',
            $SYNC_COLS
        )
    """.trimIndent()

    // ─── EXTRAS ──────────────────────────────────────────────────────────────

    val EXTRAS = """
        CREATE TABLE IF NOT EXISTS extras (
            id TEXT PRIMARY KEY,
            name TEXT NOT NULL DEFAULT '',
            price REAL NOT NULL DEFAULT 0.0,
            product_id INTEGER NOT NULL DEFAULT 0,
            type TEXT NOT NULL DEFAULT 'extra',
            uid TEXT NOT NULL,
            created_at INTEGER NOT NULL,
            updated_at INTEGER NOT NULL,
            $SYNC_COLS
        )
    """.trimIndent()

    // ─── PROVEEDORES ─────────────────────────────────────────────────────────

    val PROVEEDORES = """
        CREATE TABLE IF NOT EXISTS proveedores (
            id TEXT PRIMARY KEY,
            nombre TEXT NOT NULL DEFAULT '',
            rnc TEXT NOT NULL DEFAULT '',
            contacto TEXT NOT NULL DEFAULT '',
            telefono TEXT NOT NULL DEFAULT '',
            email TEXT NOT NULL DEFAULT '',
            direccion TEXT NOT NULL DEFAULT '',
            rubro TEXT NOT NULL DEFAULT '',
            uid TEXT NOT NULL,
            created_at INTEGER NOT NULL,
            updated_at INTEGER NOT NULL,
            $SYNC_COLS
        )
    """.trimIndent()

    // ─── COMANDAS ────────────────────────────────────────────────────────────

    val COMANDAS = """
        CREATE TABLE IF NOT EXISTS comandas (
            id TEXT PRIMARY KEY,
            mesa_name TEXT NOT NULL DEFAULT 'General',
            status TEXT NOT NULL DEFAULT 'Pendiente',
            created_at INTEGER NOT NULL,
            turno_id TEXT NOT NULL DEFAULT '',
            area TEXT NOT NULL DEFAULT 'Cocina',
            uid TEXT NOT NULL,
            updated_at INTEGER NOT NULL,
            $SYNC_COLS
        )
    """.trimIndent()

    val COMANDA_ITEMS = """
        CREATE TABLE IF NOT EXISTS comanda_items (
            id INTEGER PRIMARY KEY AUTOINCREMENT,
            comanda_id TEXT NOT NULL REFERENCES comandas(id) ON DELETE CASCADE,
            product_name TEXT NOT NULL,
            quantity INTEGER NOT NULL DEFAULT 1,
            notes TEXT NOT NULL DEFAULT '',
            course_type TEXT NOT NULL DEFAULT '',
            $SYNC_COLS
        )
    """.trimIndent()

    // ─── INVOICES ────────────────────────────────────────────────────────────

    val INVOICES = """
        CREATE TABLE IF NOT EXISTS invoices (
            invoice_number TEXT PRIMARY KEY,
            ncf TEXT NOT NULL DEFAULT '',
            total REAL NOT NULL DEFAULT 0.0,
            subtotal_pre_tax REAL NOT NULL DEFAULT 0.0,
            tax_amount REAL NOT NULL DEFAULT 0.0,
            payment_method TEXT NOT NULL DEFAULT '',
            received_amount REAL NOT NULL DEFAULT 0.0,
            change_amount REAL NOT NULL DEFAULT 0.0,
            note TEXT NOT NULL DEFAULT '',
            surcharge_amount REAL NOT NULL DEFAULT 0.0,
            surcharge_percent REAL NOT NULL DEFAULT 0.0,
            turno_id TEXT NOT NULL DEFAULT '',
            timestamp INTEGER NOT NULL,
            discount_label TEXT NOT NULL DEFAULT '',
            discount_amount REAL NOT NULL DEFAULT 0.0,
            tip_label TEXT NOT NULL DEFAULT '',
            tip_amount REAL NOT NULL DEFAULT 0.0,
            customer_id TEXT NOT NULL DEFAULT '',
            customer_name TEXT NOT NULL DEFAULT '',
            customer_rnc TEXT NOT NULL DEFAULT '',
            customer_phone TEXT NOT NULL DEFAULT '',
            status TEXT NOT NULL DEFAULT 'COMPLETED',
            diner_names TEXT NOT NULL DEFAULT '',
            delivery_address TEXT NOT NULL DEFAULT '',
            delivery_phone TEXT NOT NULL DEFAULT '',
            delivery_notes TEXT NOT NULL DEFAULT '',
            delivery_status TEXT NOT NULL DEFAULT '',
            order_number TEXT NOT NULL DEFAULT '',
            $SYNC_COLS
        )
    """.trimIndent()

    val INVOICE_ITEMS = """
        CREATE TABLE IF NOT EXISTS invoice_items (
            id INTEGER PRIMARY KEY AUTOINCREMENT,
            invoice_number TEXT NOT NULL REFERENCES invoices(invoice_number) ON DELETE CASCADE,
            product_id INTEGER NOT NULL DEFAULT 0,
            product_name TEXT NOT NULL,
            price REAL NOT NULL DEFAULT 0.0,
            tax_percent REAL NOT NULL DEFAULT 18.0,
            quantity INTEGER NOT NULL DEFAULT 1,
            extras_cost REAL NOT NULL DEFAULT 0.0,
            extras_note TEXT NOT NULL DEFAULT '',
            diner_index INTEGER NOT NULL DEFAULT 0,
            weight_quantity REAL NOT NULL DEFAULT 0.0,
            modifier_data TEXT NOT NULL DEFAULT '',
            course_type TEXT NOT NULL DEFAULT '',
            $SYNC_COLS
        )
    """.trimIndent()

    val INVOICE_PAYMENT_SPLITS = """
        CREATE TABLE IF NOT EXISTS invoice_payment_splits (
            id INTEGER PRIMARY KEY AUTOINCREMENT,
            invoice_number TEXT NOT NULL REFERENCES invoices(invoice_number) ON DELETE CASCADE,
            method TEXT NOT NULL,
            amount REAL NOT NULL,
            percentage REAL NOT NULL DEFAULT 0.0,
            $SYNC_COLS
        )
    """.trimIndent()

    // ─── EMPLOYEES ───────────────────────────────────────────────────────────

    val EMPLOYEES = """
        CREATE TABLE IF NOT EXISTS employees (
            id TEXT PRIMARY KEY,
            name TEXT NOT NULL DEFAULT '',
            position TEXT NOT NULL DEFAULT '',
            phone TEXT NOT NULL DEFAULT '',
            email TEXT NOT NULL DEFAULT '',
            hire_date TEXT NOT NULL DEFAULT '',
            active INTEGER NOT NULL DEFAULT 1,
            hourly_rate REAL NOT NULL DEFAULT 0.0,
            commission_percent REAL NOT NULL DEFAULT 0.0,
            clocked_in INTEGER NOT NULL DEFAULT 0,
            last_clock_in INTEGER NOT NULL DEFAULT 0,
            total_worked_minutes INTEGER NOT NULL DEFAULT 0,
            uid TEXT NOT NULL,
            created_at INTEGER NOT NULL,
            updated_at INTEGER NOT NULL,
            $SYNC_COLS
        )
    """.trimIndent()

    // ─── CREDIT ACCOUNTS ─────────────────────────────────────────────────────

    val CREDIT_ORDERS = """
        CREATE TABLE IF NOT EXISTS credit_orders (
            id TEXT PRIMARY KEY,
            client_id TEXT NOT NULL,
            note TEXT NOT NULL DEFAULT '',
            created_at INTEGER NOT NULL,
            date_key TEXT NOT NULL,
            $SYNC_COLS
        )
    """.trimIndent()

    val CREDIT_ORDER_ITEMS = """
        CREATE TABLE IF NOT EXISTS credit_order_items (
            id INTEGER PRIMARY KEY AUTOINCREMENT,
            order_id TEXT NOT NULL REFERENCES credit_orders(id) ON DELETE CASCADE,
            product_id INTEGER NOT NULL,
            name TEXT NOT NULL,
            unit_price REAL NOT NULL DEFAULT 0.0,
            quantity INTEGER NOT NULL DEFAULT 1,
            note TEXT NOT NULL DEFAULT '',
            $SYNC_COLS
        )
    """.trimIndent()

    val CREDIT_PAYMENTS = """
        CREATE TABLE IF NOT EXISTS credit_payments (
            id TEXT PRIMARY KEY,
            client_id TEXT NOT NULL,
            amount REAL NOT NULL DEFAULT 0.0,
            method TEXT NOT NULL DEFAULT '',
            note TEXT NOT NULL DEFAULT '',
            created_at INTEGER NOT NULL,
            date_key TEXT NOT NULL,
            $SYNC_COLS
        )
    """.trimIndent()

    // ─── MODIFIERS ───────────────────────────────────────────────────────────

    val MODIFIER_GROUPS = """
        CREATE TABLE IF NOT EXISTS modifier_groups (
            id TEXT PRIMARY KEY,
            name TEXT NOT NULL,
            required INTEGER NOT NULL DEFAULT 0,
            max_selections INTEGER NOT NULL DEFAULT 1,
            created_at INTEGER NOT NULL,
            updated_at INTEGER NOT NULL,
            $SYNC_COLS
        )
    """.trimIndent()

    val MODIFIER_OPTIONS = """
        CREATE TABLE IF NOT EXISTS modifier_options (
            id TEXT PRIMARY KEY,
            group_id TEXT NOT NULL REFERENCES modifier_groups(id) ON DELETE CASCADE,
            name TEXT NOT NULL,
            price REAL NOT NULL DEFAULT 0.0,
            $SYNC_COLS
        )
    """.trimIndent()

    // ─── RECIPES ─────────────────────────────────────────────────────────────

    val RECIPES = """
        CREATE TABLE IF NOT EXISTS recipes (
            id TEXT PRIMARY KEY,
            product_id INTEGER NOT NULL,
            product_name TEXT NOT NULL DEFAULT '',
            servings INTEGER NOT NULL DEFAULT 1,
            created_at INTEGER NOT NULL,
            updated_at INTEGER NOT NULL,
            $SYNC_COLS
        )
    """.trimIndent()

    val RECIPE_INGREDIENTS = """
        CREATE TABLE IF NOT EXISTS recipe_ingredients (
            id INTEGER PRIMARY KEY AUTOINCREMENT,
            recipe_id TEXT NOT NULL REFERENCES recipes(id) ON DELETE CASCADE,
            product_id INTEGER NOT NULL,
            product_name TEXT NOT NULL,
            quantity REAL NOT NULL DEFAULT 0.0,
            unit TEXT NOT NULL DEFAULT 'unidad',
            $SYNC_COLS
        )
    """.trimIndent()

    // ─── PURCHASE ORDERS ─────────────────────────────────────────────────────

    val PURCHASE_ORDERS = """
        CREATE TABLE IF NOT EXISTS purchase_orders (
            id TEXT PRIMARY KEY,
            provider_name TEXT NOT NULL,
            status TEXT NOT NULL DEFAULT 'PENDIENTE',
            notes TEXT NOT NULL DEFAULT '',
            created_at INTEGER NOT NULL,
            updated_at INTEGER NOT NULL,
            $SYNC_COLS
        )
    """.trimIndent()

    val PURCHASE_ORDER_ITEMS = """
        CREATE TABLE IF NOT EXISTS purchase_order_items (
            id INTEGER PRIMARY KEY AUTOINCREMENT,
            order_id TEXT NOT NULL REFERENCES purchase_orders(id) ON DELETE CASCADE,
            product_id INTEGER NOT NULL,
            product_name TEXT NOT NULL,
            quantity INTEGER NOT NULL DEFAULT 1,
            unit_price REAL NOT NULL DEFAULT 0.0,
            $SYNC_COLS
        )
    """.trimIndent()

    // ─── QUOTES ──────────────────────────────────────────────────────────────

    val QUOTES = """
        CREATE TABLE IF NOT EXISTS quotes (
            id TEXT PRIMARY KEY,
            customer_id TEXT NOT NULL DEFAULT '',
            customer_name TEXT NOT NULL DEFAULT '',
            customer_email TEXT NOT NULL DEFAULT '',
            customer_phone TEXT NOT NULL DEFAULT '',
            valid_until TEXT NOT NULL DEFAULT '',
            notes TEXT NOT NULL DEFAULT '',
            status TEXT NOT NULL DEFAULT 'BORRADOR',
            created_at INTEGER NOT NULL,
            updated_at INTEGER NOT NULL,
            $SYNC_COLS
        )
    """.trimIndent()

    val QUOTE_ITEMS = """
        CREATE TABLE IF NOT EXISTS quote_items (
            id INTEGER PRIMARY KEY AUTOINCREMENT,
            quote_id TEXT NOT NULL REFERENCES quotes(id) ON DELETE CASCADE,
            product_id INTEGER NOT NULL,
            product_name TEXT NOT NULL,
            unit_price REAL NOT NULL DEFAULT 0.0,
            quantity INTEGER NOT NULL DEFAULT 1,
            tax_percent REAL NOT NULL DEFAULT 18.0,
            $SYNC_COLS
        )
    """.trimIndent()

    // ─── HELD ORDERS ─────────────────────────────────────────────────────────

    val HELD_ORDERS = """
        CREATE TABLE IF NOT EXISTS held_orders (
            id TEXT PRIMARY KEY,
            label TEXT NOT NULL,
            discount_label TEXT NOT NULL DEFAULT '',
            discount_amount REAL NOT NULL DEFAULT 0.0,
            client_id TEXT NOT NULL DEFAULT '',
            client_name TEXT NOT NULL DEFAULT '',
            timestamp INTEGER NOT NULL,
            $SYNC_COLS
        )
    """.trimIndent()

    val HELD_ORDER_ITEMS = """
        CREATE TABLE IF NOT EXISTS held_order_items (
            id INTEGER PRIMARY KEY AUTOINCREMENT,
            held_order_id TEXT NOT NULL REFERENCES held_orders(id) ON DELETE CASCADE,
            product_id INTEGER NOT NULL,
            product_name TEXT NOT NULL,
            price REAL NOT NULL DEFAULT 0.0,
            tax_percent REAL NOT NULL DEFAULT 18.0,
            quantity INTEGER NOT NULL DEFAULT 1,
            extras_cost REAL NOT NULL DEFAULT 0.0,
            extras_note TEXT NOT NULL DEFAULT '',
            diner_index INTEGER NOT NULL DEFAULT 0,
            weight_quantity REAL NOT NULL DEFAULT 0.0,
            modifier_data TEXT NOT NULL DEFAULT '',
            course_type TEXT NOT NULL DEFAULT '',
            $SYNC_COLS
        )
    """.trimIndent()

    // ─── INVENTORY ADJUSTMENTS ──────────────────────────────────────────────

    val INVENTORY_ADJUSTMENTS = """
        CREATE TABLE IF NOT EXISTS inventory_adjustments (
            id TEXT PRIMARY KEY,
            product_id INTEGER NOT NULL,
            product_name TEXT NOT NULL DEFAULT '',
            previous_stock INTEGER NOT NULL DEFAULT 0,
            new_stock INTEGER NOT NULL DEFAULT 0,
            delta INTEGER NOT NULL DEFAULT 0,
            reason TEXT NOT NULL DEFAULT '',
            user_id TEXT NOT NULL DEFAULT '',
            user_name TEXT NOT NULL DEFAULT '',
            created_at INTEGER NOT NULL,
            $SYNC_COLS
        )
    """.trimIndent()

    // ─── AUDIT LOG ───────────────────────────────────────────────────────────

    val AUDIT_LOG = """
        CREATE TABLE IF NOT EXISTS audit_log (
            id TEXT PRIMARY KEY,
            module TEXT NOT NULL,
            action TEXT NOT NULL,
            detail TEXT NOT NULL DEFAULT '',
            actor_id TEXT NOT NULL DEFAULT '',
            actor_name TEXT NOT NULL DEFAULT 'Sistema',
            actor_role TEXT NOT NULL DEFAULT 'SYSTEM',
            turno_id TEXT NOT NULL DEFAULT '',
            level TEXT NOT NULL DEFAULT 'INFO',
            created_at INTEGER NOT NULL,
            $SYNC_COLS
        )
    """.trimIndent()

    // ─── RESERVACIONES ───────────────────────────────────────────────────────

    val RESERVACIONES = """
        CREATE TABLE IF NOT EXISTS reservaciones (
            id TEXT PRIMARY KEY,
            cliente_nombre TEXT NOT NULL DEFAULT '',
            cliente_telefono TEXT NOT NULL DEFAULT '',
            cliente_personas INTEGER NOT NULL DEFAULT 1,
            fecha TEXT NOT NULL DEFAULT '',
            hora TEXT NOT NULL DEFAULT '',
            notas TEXT NOT NULL DEFAULT '',
            mesa_id INTEGER,
            estado TEXT NOT NULL DEFAULT 'PENDIENTE',
            created_at INTEGER NOT NULL,
            updated_at INTEGER NOT NULL,
            $SYNC_COLS
        )
    """.trimIndent()

    // ─── TURNOS ──────────────────────────────────────────────────────────────

    val TURNOS = """
        CREATE TABLE IF NOT EXISTS turnos (
            id TEXT PRIMARY KEY,
            user_id TEXT NOT NULL,
            user_name TEXT NOT NULL,
            initial_amount REAL NOT NULL DEFAULT 0.0,
            start_time INTEGER NOT NULL,
            end_time INTEGER,
            is_closed INTEGER NOT NULL DEFAULT 0,
            $SYNC_COLS
        )
    """.trimIndent()

    val GASTOS = """
        CREATE TABLE IF NOT EXISTS gastos (
            id TEXT PRIMARY KEY,
            turno_id TEXT NOT NULL REFERENCES turnos(id) ON DELETE CASCADE,
            description TEXT NOT NULL,
            amount REAL NOT NULL DEFAULT 0.0,
            user_id TEXT NOT NULL DEFAULT '',
            user_name TEXT NOT NULL DEFAULT '',
            created_at INTEGER NOT NULL,
            $SYNC_COLS
        )
    """.trimIndent()

    val CAJA_MOVIMIENTOS = """
        CREATE TABLE IF NOT EXISTS caja_movimientos (
            id TEXT PRIMARY KEY,
            turno_id TEXT NOT NULL REFERENCES turnos(id) ON DELETE CASCADE,
            tipo TEXT NOT NULL,
            description TEXT NOT NULL,
            amount REAL NOT NULL DEFAULT 0.0,
            user_id TEXT NOT NULL DEFAULT '',
            user_name TEXT NOT NULL DEFAULT '',
            created_at INTEGER NOT NULL,
            $SYNC_COLS
        )
    """.trimIndent()

    // ─── MESAS ───────────────────────────────────────────────────────────────

    val MESAS = """
        CREATE TABLE IF NOT EXISTS mesas (
            id INTEGER NOT NULL,
            turno_id TEXT NOT NULL,
            name TEXT NOT NULL,
            is_occupied INTEGER NOT NULL DEFAULT 0,
            opened_at INTEGER NOT NULL DEFAULT 0,
            x_pos REAL NOT NULL DEFAULT -1.0,
            y_pos REAL NOT NULL DEFAULT -1.0,
            shape TEXT NOT NULL DEFAULT 'rectangle',
            table_width INTEGER NOT NULL DEFAULT 120,
            table_height INTEGER NOT NULL DEFAULT 80,
            waiter_name TEXT NOT NULL DEFAULT '',
            $SYNC_COLS,
            PRIMARY KEY (id, turno_id)
        )
    """.trimIndent()

    // ─── ROLE PERMISSIONS ────────────────────────────────────────────────────

    val ROLE_PERMISSIONS = """
        CREATE TABLE IF NOT EXISTS role_permissions (
            role_name TEXT NOT NULL,
            permission_name TEXT NOT NULL,
            $SYNC_COLS,
            PRIMARY KEY (role_name, permission_name)
        )
    """.trimIndent()

    // ─── SYNC QUEUE ──────────────────────────────────────────────────────────

    val SYNC_QUEUE = """
        CREATE TABLE IF NOT EXISTS sync_queue (
            id INTEGER PRIMARY KEY AUTOINCREMENT,
            table_name TEXT NOT NULL,
            row_id TEXT NOT NULL,
            operation TEXT NOT NULL,
            payload TEXT NOT NULL DEFAULT '',
            status TEXT NOT NULL DEFAULT 'pending',
            attempts INTEGER NOT NULL DEFAULT 0,
            last_error TEXT,
            created_at INTEGER NOT NULL,
            $SYNC_COLS_NO_DEFAULT
        )
    """.trimIndent()

    val TABLES = listOf(
        COMPANY_SETTINGS, VISUAL_SETTINGS, SALES_SETTINGS,
        PAYMENT_METHODS, PRINT_SETTINGS, NOTIFICATION_SETTINGS,
        LICENSE_SETTINGS, SERVER_SETTINGS, SYSTEM_SETTINGS,
        ADMIN_CARDS, FISCAL_SEQUENCES, PRODUCTS, CATEGORIES,
        CLIENTES, USUARIOS, EXTRAS, PROVEEDORES,
        COMANDA_ITEMS, COMANDAS,
        INVOICE_ITEMS, INVOICE_PAYMENT_SPLITS, INVOICES,
        EMPLOYEES,
        CREDIT_ORDER_ITEMS, CREDIT_ORDERS, CREDIT_PAYMENTS,
        MODIFIER_OPTIONS, MODIFIER_GROUPS,
        RECIPE_INGREDIENTS, RECIPES,
        PURCHASE_ORDER_ITEMS, PURCHASE_ORDERS,
        QUOTE_ITEMS, QUOTES,
        HELD_ORDER_ITEMS, HELD_ORDERS,
        INVENTORY_ADJUSTMENTS, AUDIT_LOG, RESERVACIONES,
        GASTOS, CAJA_MOVIMIENTOS, TURNOS, MESAS,
        ROLE_PERMISSIONS, SYNC_QUEUE
    )
}
