const { useMemo, useState } = React;

const MEDIO_PAGO_OPTIONS = [
  "CREDITO",
  "DEBITO",
  "EFECTIVO",
  "CUENTACORRIENTE",
  "MERCADOPAGO",
  "TRANSFERENCIABANCARIA",
  "CHEQUEFISICO",
  "OPERACIONINTERNA",
];

const TIPO_VENTA_OPTIONS = [
  "CREDITO",
  "DEBITO",
  "EFECTIVO",
  "EVENTOS",
  "MENU_DEL_DIA",
  "MERCADOPAGO",
];

const MODO_EVENTO_OPTIONS = [
  "LINK_DE_PAGO",
  "TARJETA_CORPORATIVA",
  "ORDEN_DE_COMPRA",
  "FORMACION",
];

const CONTRATO_OPTIONS = ["MENSUAL", "HORAS_EXTRA", "SEMANAL", "BONO", "EVENTOS"];

const SEED_SQL = `-- Seed base para Gestion Comedores (H2)
MERGE INTO comedores (id, name) KEY (id) VALUES
  (1, 'Comedor Central');

MERGE INTO punto_de_venta (id, comedor_id) KEY (id) VALUES
  (1, 1);

MERGE INTO empleados (id, tax_id, nombre) KEY (id) VALUES
  (1, '20-11111111-3', 'Usuario Administracion'),
  (2, '20-22222222-3', 'Empleado Demo');

MERGE INTO usuarios (id, rol, passhash) KEY (id) VALUES
  (1, 'ADMINISTRACION', 'demo-admin-hash');

MERGE INTO bancos (id, nombre, active) KEY (id) VALUES
  (1, 'Banco Demo', TRUE);

MERGE INTO proveedores (id, name, tax_id) KEY (id) VALUES
  (1, 'Proveedor Demo', '30-12345678-9');`;

function sanitizeBaseUrl(raw) {
  return (raw || "").trim().replace(/\/+$/, "");
}

function getDefaultBaseUrl() {
  if (typeof window === "undefined") return "http://localhost:8080";
  return window.location.origin;
}

function compactObject(source) {
  return Object.fromEntries(
    Object.entries(source).filter(([, value]) => value !== undefined)
  );
}

function buildSummary(actionId, data) {
  if (Array.isArray(data)) {
    if (actionId === "caja-get-cierres") {
      const borrador = data.filter((c) => c?.estado === "BORRADOR").length;
      const cerrado = data.filter((c) => c?.estado === "CERRADO").length;
      const anulado = data.filter((c) => c?.estado === "ANULADO").length;
      return `Se encontraron ${data.length} cierres. BORRADOR: ${borrador} | CERRADO: ${cerrado} | ANULADO: ${anulado}.`;
    }

    if (actionId === "caja-get-movimientos") {
      const aportes = data.filter((m) => m?.categoria === "APORTE").length;
      const cierres = data.filter((m) => m?.categoria === "CIERRE").length;
      return `Se encontraron ${data.length} movimientos. Aportes: ${aportes} | Desde cierres: ${cierres}.`;
    }

    return `Se encontraron ${data.length} registros.`;
  }

  if (!data || typeof data !== "object") {
    return "Operación completada correctamente.";
  }

  if (data.error) {
    return `No se pudo completar la acción: ${String(data.error)}`;
  }

  if (actionId.startsWith("caja-")) {
    if (data.id && data.estado) {
      const lineas = Array.isArray(data.lineas) ? data.lineas.length : 0;
      const movimientos = Array.isArray(data.movimientos) ? data.movimientos.length : 0;
      return `Cierre #${data.id} en estado "${data.estado}". Total: ${data.totalCierre ?? "-"} | Líneas: ${lineas} | Movimientos: ${movimientos}.`;
    }
    if (data.categoria) {
      return `Movimiento de caja #${data.id ?? "-"} registrado en categoría "${data.categoria}" por ${data.monto ?? "-"} (${data.estadoMovimientoCaja ?? "sin estado"}).`;
    }
  }

  if (actionId.startsWith("sueldos-")) {
    return `Pago de sueldo #${data.id ?? "-"} en estado "${data.estado ?? "-"}". Monto total: ${data.montoTotal ?? "-"}.`;
  }

  if (actionId.startsWith("facturas-")) {
    return `Factura #${data.id ?? "-"} (${data.numeroFactura ?? "-"}) en estado "${data.estado ?? "-"}".`;
  }

  return "Operación completada correctamente.";
}

async function callApi({ method, baseUrl, path, query, body }) {
  const url = new URL(`${sanitizeBaseUrl(baseUrl)}${path}`);
  Object.entries(query || {}).forEach(([key, value]) => {
    if (value !== undefined && value !== null && String(value).trim() !== "") {
      url.searchParams.set(key, String(value));
    }
  });

  const options = {
    method,
    headers: {
      Accept: "application/json",
    },
  };

  if (body && Object.keys(body).length > 0) {
    options.headers["Content-Type"] = "application/json";
    options.body = JSON.stringify(body);
  }

  const response = await fetch(url.toString(), options);
  const rawText = await response.text();
  let parsed = null;

  if (rawText) {
    try {
      parsed = JSON.parse(rawText);
    } catch (e) {
      parsed = { message: rawText };
    }
  } else {
    parsed = {};
  }

  if (!response.ok) {
    const message = parsed?.error || parsed?.message || "No se pudo completar la operación.";
    throw new Error(message);
  }

  return parsed;
}

async function copyToClipboard(text) {
  if (navigator?.clipboard?.writeText) {
    await navigator.clipboard.writeText(text);
    return;
  }

  const textArea = document.createElement("textarea");
  textArea.value = text;
  textArea.setAttribute("readonly", "");
  textArea.style.position = "absolute";
  textArea.style.left = "-9999px";
  document.body.appendChild(textArea);
  textArea.select();
  document.execCommand("copy");
  document.body.removeChild(textArea);
}

function EndpointCard({ action, baseUrl }) {
  const [form, setForm] = useState(action.getInitialState());
  const [loading, setLoading] = useState(false);
  const [resultText, setResultText] = useState("");
  const [resultOk, setResultOk] = useState(true);
  const [rawResult, setRawResult] = useState("");

  const visibleFields = useMemo(() => action.getVisibleFields(form), [action, form]);

  const onFieldChange = (name, value) => {
    setForm((prev) => ({ ...prev, [name]: value }));
  };

  const onReset = () => {
    setForm(action.getInitialState());
    setResultText("");
    setRawResult("");
    setResultOk(true);
  };

  const onSubmit = async (evt) => {
    evt.preventDefault();
    setLoading(true);
    setResultText("");
    setRawResult("");

    try {
      const payload = action.buildPayload(form);
      const data = await callApi({
        method: action.method,
        baseUrl,
        path: action.buildPath(form),
        query: payload.query,
        body: payload.body,
      });
      setResultText(buildSummary(action.id, data));
      setResultOk(true);
      setRawResult(JSON.stringify(data, null, 2));
    } catch (err) {
      setResultText(String(err.message || "No se pudo completar la operación."));
      setResultOk(false);
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="endpoint-card">
      <div className="endpoint-top">
        <h3 className="endpoint-name">{action.name}</h3>
        <span className={`method-badge ${action.method.toLowerCase()}`}>{action.method}</span>
      </div>
      <div className="path">{action.pathLabel}</div>
      <div className="helper">{action.helper}</div>

      <form onSubmit={onSubmit}>
        <div className="field-grid">
          {visibleFields.map((field) => (
            <Field
              key={field.name}
              field={field}
              value={form[field.name]}
              onChange={onFieldChange}
            />
          ))}
        </div>

        <div className="btn-row">
          <button className="btn primary" type="submit" disabled={loading}>
            {loading ? "Procesando..." : "Probar"}
          </button>
          <button className="btn secondary" type="button" onClick={onReset} disabled={loading}>
            Limpiar
          </button>
        </div>
      </form>

      {resultText && (
        <div className="result-panel">
          <h4 className="result-title">Resultado</h4>
          <p className={`result-text ${resultOk ? "ok" : "error"}`}>{resultText}</p>
          {rawResult && (
            <details>
              <summary>Ver detalle completo</summary>
              <pre className="raw-box">{rawResult}</pre>
            </details>
          )}
        </div>
      )}
    </div>
  );
}

function Field({ field, value, onChange }) {
  const id = `field-${field.name}`;
  if (field.type === "textarea") {
    return (
      <div>
        <label htmlFor={id}>{field.label}</label>
        <textarea
          id={id}
          value={value ?? ""}
          onChange={(e) => onChange(field.name, e.target.value)}
          placeholder={field.placeholder || ""}
        />
      </div>
    );
  }

  if (field.type === "select") {
    return (
      <div>
        <label htmlFor={id}>{field.label}</label>
        <select id={id} value={value ?? ""} onChange={(e) => onChange(field.name, e.target.value)}>
          <option value="">Seleccionar...</option>
          {field.options.map((opt) => (
            <option key={opt} value={opt}>
              {opt}
            </option>
          ))}
        </select>
      </div>
    );
  }

  return (
    <div>
      <label htmlFor={id}>{field.label}</label>
      <input
        id={id}
        type={field.type || "text"}
        value={value ?? ""}
        onChange={(e) => onChange(field.name, e.target.value)}
        placeholder={field.placeholder || ""}
      />
    </div>
  );
}

const actions = [
  {
    id: "caja-crear-cierre",
    module: "Caja",
    method: "POST",
    name: "Crear cierre de caja",
    pathLabel: "/api/cajas/cierres?usuarioId={usuarioId}",
    helper: "Crea un cierre nuevo para una fecha, comedor y punto de venta.",
    getInitialState: () => ({
      usuarioId: "1",
      comedorId: "1",
      puntoVentaId: "1",
      fechaOperacion: new Date().toISOString().slice(0, 10),
      observaciones: "",
    }),
    getVisibleFields: () => [
      { name: "usuarioId", label: "Usuario ID", type: "number" },
      { name: "comedorId", label: "Comedor ID", type: "number" },
      { name: "puntoVentaId", label: "Punto de venta ID", type: "number" },
      { name: "fechaOperacion", label: "Fecha de operación", type: "date" },
      { name: "observaciones", label: "Observaciones", type: "textarea" },
    ],
    buildPath: () => "/api/cajas/cierres",
    buildPayload: (f) => ({
      query: { usuarioId: f.usuarioId },
      body: {
        comedorId: Number(f.comedorId),
        puntoVentaId: Number(f.puntoVentaId),
        fechaOperacion: f.fechaOperacion,
        observaciones: f.observaciones || null,
      },
    }),
  },
  {
    id: "caja-get-cierre",
    module: "Caja",
    method: "GET",
    name: "Ver cierre de caja",
    pathLabel: "/api/cajas/cierres/{id}",
    helper: "Muestra el detalle completo del cierre.",
    getInitialState: () => ({ cierreId: "1" }),
    getVisibleFields: () => [{ name: "cierreId", label: "Cierre ID", type: "number" }],
    buildPath: (f) => `/api/cajas/cierres/${f.cierreId}`,
    buildPayload: () => ({ query: {}, body: null }),
  },
  {
    id: "caja-get-cierres",
    module: "Caja",
    method: "GET",
    name: "Ver todos los cierres",
    pathLabel: "/api/cajas/cierres",
    helper: "Muestra todos los cierres registrados.",
    getInitialState: () => ({}),
    getVisibleFields: () => [],
    buildPath: () => "/api/cajas/cierres",
    buildPayload: () => ({ query: {}, body: null }),
  },
  {
    id: "caja-agregar-linea",
    module: "Caja",
    method: "POST",
    name: "Agregar línea a un cierre",
    pathLabel: "/api/cajas/cierres/{id}/lineas?usuarioId={usuarioId}",
    helper: "Agrega una línea de venta al cierre seleccionado.",
    getInitialState: () => ({
      cierreId: "1",
      usuarioId: "1",
      tipoVenta: "EFECTIVO",
      monto: "1000.00",
      precioMenuUnitarioSnapshot: "",
      cobradoEvento: "true",
      modoPagoEvento: "LINK_DE_PAGO",
      numeroOperacion: "",
      numeroOrdenEvento: "",
      cantidadPaxEvento: "",
      lugarPisoEvento: "",
    }),
    getVisibleFields: (f) => {
      const isEvento = f.tipoVenta === "EVENTOS";
      const isMenu = f.tipoVenta === "MENU_DEL_DIA";
      const fields = [
        { name: "cierreId", label: "Cierre ID", type: "number" },
        { name: "usuarioId", label: "Usuario ID", type: "number" },
        { name: "tipoVenta", label: "Tipo de venta", type: "select", options: TIPO_VENTA_OPTIONS },
        { name: "monto", label: "Monto", type: "number" },
      ];
      if (isMenu) fields.push({ name: "precioMenuUnitarioSnapshot", label: "Precio unitario menú", type: "number" });
      if (isEvento) {
        fields.push({ name: "cobradoEvento", label: "¿Cobrado?", type: "select", options: ["true", "false"] });
        fields.push({ name: "modoPagoEvento", label: "Modo de pago evento", type: "select", options: MODO_EVENTO_OPTIONS });
        fields.push({ name: "numeroOrdenEvento", label: "Número orden evento", type: "text" });
        fields.push({ name: "cantidadPaxEvento", label: "Cantidad pax", type: "number" });
        fields.push({ name: "lugarPisoEvento", label: "Lugar/piso evento", type: "text" });
      }
      fields.push({ name: "numeroOperacion", label: "Número de operación", type: "text" });
      return fields;
    },
    buildPath: (f) => `/api/cajas/cierres/${f.cierreId}/lineas`,
    buildPayload: (f) => {
      const isEvento = f.tipoVenta === "EVENTOS";
      const isMenu = f.tipoVenta === "MENU_DEL_DIA";
      return {
        query: { usuarioId: f.usuarioId },
        body: {
          tipoVenta: f.tipoVenta,
          monto: Number(f.monto),
          precioMenuUnitarioSnapshot: isMenu && f.precioMenuUnitarioSnapshot ? Number(f.precioMenuUnitarioSnapshot) : null,
          cobradoEvento: isEvento ? f.cobradoEvento === "true" : null,
          modoPagoEvento: isEvento ? f.modoPagoEvento || null : null,
          numeroOperacion: f.numeroOperacion || null,
          numeroOrdenEvento: isEvento ? f.numeroOrdenEvento || null : null,
          cantidadPaxEvento: isEvento && f.cantidadPaxEvento ? Number(f.cantidadPaxEvento) : null,
          lugarPisoEvento: isEvento ? f.lugarPisoEvento || null : null,
        },
      };
    },
  },
  {
    id: "caja-reemplazar-linea",
    module: "Caja",
    method: "PUT",
    name: "Reemplazar línea",
    pathLabel: "/api/cajas/cierres/{id}/lineas/reemplazar?usuarioId={usuarioId}",
    helper: "Anula una línea anterior y crea una nueva en su lugar.",
    getInitialState: () => ({
      cierreId: "1",
      usuarioId: "1",
      lineaIdOriginal: "1",
      tipoVenta: "EVENTOS",
      monto: "1000.00",
      precioMenuUnitarioSnapshot: "",
      cobradoEvento: "true",
      modoPagoEvento: "LINK_DE_PAGO",
      numeroOperacion: "",
      numeroOrdenEvento: "EVT-001",
      cantidadPaxEvento: "1",
      lugarPisoEvento: "",
      motivo: "Ajuste de carga",
    }),
    getVisibleFields: (f) => {
      const isEvento = f.tipoVenta === "EVENTOS";
      const isMenu = f.tipoVenta === "MENU_DEL_DIA";
      const fields = [
        { name: "cierreId", label: "Cierre ID", type: "number" },
        { name: "usuarioId", label: "Usuario ID", type: "number" },
        { name: "lineaIdOriginal", label: "Línea original ID", type: "number" },
        { name: "tipoVenta", label: "Tipo de venta", type: "select", options: TIPO_VENTA_OPTIONS },
        { name: "monto", label: "Monto", type: "number" },
      ];
      if (isMenu) fields.push({ name: "precioMenuUnitarioSnapshot", label: "Precio unitario menú", type: "number" });
      if (isEvento) {
        fields.push({ name: "cobradoEvento", label: "¿Cobrado?", type: "select", options: ["true", "false"] });
        fields.push({ name: "modoPagoEvento", label: "Modo pago evento", type: "select", options: MODO_EVENTO_OPTIONS });
        fields.push({ name: "numeroOrdenEvento", label: "Número orden evento", type: "text" });
        fields.push({ name: "cantidadPaxEvento", label: "Cantidad pax", type: "number" });
        fields.push({ name: "lugarPisoEvento", label: "Lugar/piso evento", type: "text" });
      }
      fields.push({ name: "numeroOperacion", label: "Número operación", type: "text" });
      fields.push({ name: "motivo", label: "Motivo del reemplazo", type: "text" });
      return fields;
    },
    buildPath: (f) => `/api/cajas/cierres/${f.cierreId}/lineas/reemplazar`,
    buildPayload: (f) => {
      const isEvento = f.tipoVenta === "EVENTOS";
      const isMenu = f.tipoVenta === "MENU_DEL_DIA";
      return {
        query: { usuarioId: f.usuarioId },
        body: {
          lineaIdOriginal: Number(f.lineaIdOriginal),
          tipoVenta: f.tipoVenta,
          monto: Number(f.monto),
          precioMenuUnitarioSnapshot: isMenu && f.precioMenuUnitarioSnapshot ? Number(f.precioMenuUnitarioSnapshot) : null,
          cobradoEvento: isEvento ? f.cobradoEvento === "true" : null,
          modoPagoEvento: isEvento ? f.modoPagoEvento || null : null,
          numeroOperacion: f.numeroOperacion || null,
          numeroOrdenEvento: isEvento ? f.numeroOrdenEvento || null : null,
          cantidadPaxEvento: isEvento && f.cantidadPaxEvento ? Number(f.cantidadPaxEvento) : null,
          lugarPisoEvento: isEvento ? f.lugarPisoEvento || null : null,
          motivo: f.motivo,
        },
      };
    },
  },
  {
    id: "caja-anular-linea",
    module: "Caja",
    method: "PUT",
    name: "Anular línea",
    pathLabel: "/api/cajas/cierres/{id}/lineas/anular?usuarioId={usuarioId}",
    helper: "Anula una línea específica de un cierre.",
    getInitialState: () => ({
      cierreId: "1",
      usuarioId: "1",
      lineaId: "1",
      motivo: "Error de carga",
    }),
    getVisibleFields: () => [
      { name: "cierreId", label: "Cierre ID", type: "number" },
      { name: "usuarioId", label: "Usuario ID", type: "number" },
      { name: "lineaId", label: "Línea ID", type: "number" },
      { name: "motivo", label: "Motivo", type: "text" },
    ],
    buildPath: (f) => `/api/cajas/cierres/${f.cierreId}/lineas/anular`,
    buildPayload: (f) => ({
      query: { usuarioId: f.usuarioId },
      body: { lineaId: Number(f.lineaId), motivo: f.motivo },
    }),
  },
  {
    id: "caja-observaciones",
    module: "Caja",
    method: "PUT",
    name: "Actualizar observaciones del cierre",
    pathLabel: "/api/cajas/cierres/{id}/observaciones?usuarioId={usuarioId}",
    helper: "Cambia el texto de observaciones del cierre.",
    getInitialState: () => ({
      cierreId: "1",
      usuarioId: "1",
      observaciones: "Observaciones actualizadas desde dashboard",
    }),
    getVisibleFields: () => [
      { name: "cierreId", label: "Cierre ID", type: "number" },
      { name: "usuarioId", label: "Usuario ID", type: "number" },
      { name: "observaciones", label: "Observaciones", type: "textarea" },
    ],
    buildPath: (f) => `/api/cajas/cierres/${f.cierreId}/observaciones`,
    buildPayload: (f) => ({
      query: { usuarioId: f.usuarioId },
      body: { observaciones: f.observaciones },
    }),
  },
  {
    id: "caja-finalizar",
    module: "Caja",
    method: "PUT",
    name: "Finalizar cierre",
    pathLabel: "/api/cajas/cierres/{id}/finalizar?usuarioId={usuarioId}",
    helper: "Pasa el cierre al estado final.",
    getInitialState: () => ({ cierreId: "1", usuarioId: "1" }),
    getVisibleFields: () => [
      { name: "cierreId", label: "Cierre ID", type: "number" },
      { name: "usuarioId", label: "Usuario ID", type: "number" },
    ],
    buildPath: (f) => `/api/cajas/cierres/${f.cierreId}/finalizar`,
    buildPayload: (f) => ({ query: { usuarioId: f.usuarioId }, body: null }),
  },
  {
    id: "caja-anular-cierre",
    module: "Caja",
    method: "PUT",
    name: "Anular cierre",
    pathLabel: "/api/cajas/cierres/{id}/anular?usuarioId={usuarioId}",
    helper: "Anula el cierre completo y deja registro de motivo.",
    getInitialState: () => ({ cierreId: "1", usuarioId: "1", motivo: "Anulación administrativa" }),
    getVisibleFields: () => [
      { name: "cierreId", label: "Cierre ID", type: "number" },
      { name: "usuarioId", label: "Usuario ID", type: "number" },
      { name: "motivo", label: "Motivo", type: "text" },
    ],
    buildPath: (f) => `/api/cajas/cierres/${f.cierreId}/anular`,
    buildPayload: (f) => ({
      query: { usuarioId: f.usuarioId },
      body: { motivo: f.motivo },
    }),
  },
  {
    id: "caja-aporte",
    module: "Caja",
    method: "POST",
    name: "Registrar aporte",
    pathLabel: "/api/cajas/movimientos/aportes?usuarioId={usuarioId}",
    helper: "Registra un ingreso manual en caja.",
    getInitialState: () => ({
      usuarioId: "1",
      comedorId: "1",
      puntoVentaId: "1",
      monto: "1000.00",
      medioPago: "EFECTIVO",
      comentarios: "Aporte manual",
    }),
    getVisibleFields: () => [
      { name: "usuarioId", label: "Usuario ID", type: "number" },
      { name: "comedorId", label: "Comedor ID", type: "number" },
      { name: "puntoVentaId", label: "Punto de venta ID", type: "number" },
      { name: "monto", label: "Monto", type: "number" },
      { name: "medioPago", label: "Medio de pago", type: "select", options: MEDIO_PAGO_OPTIONS },
      { name: "comentarios", label: "Comentarios", type: "textarea" },
    ],
    buildPath: () => "/api/cajas/movimientos/aportes",
    buildPayload: (f) => ({
      query: { usuarioId: f.usuarioId },
      body: {
        comedorId: Number(f.comedorId),
        puntoVentaId: Number(f.puntoVentaId),
        monto: Number(f.monto),
        medioPago: f.medioPago,
        comentarios: f.comentarios || null,
      },
    }),
  },
  {
    id: "caja-get-movimientos",
    module: "Caja",
    method: "GET",
    name: "Ver movimientos de caja",
    pathLabel: "/api/cajas/movimientos?usuarioId={usuarioId}",
    helper: "Muestra todos los movimientos de caja, incluidos aportes.",
    getInitialState: () => ({
      usuarioId: "1",
    }),
    getVisibleFields: () => [
      { name: "usuarioId", label: "Usuario ID", type: "number" },
    ],
    buildPath: () => "/api/cajas/movimientos",
    buildPayload: (f) => ({
      query: { usuarioId: f.usuarioId },
      body: null,
    }),
  },
  {
    id: "sueldos-crear",
    module: "Sueldos",
    method: "POST",
    name: "Crear pago de sueldo",
    pathLabel: "/api/sueldos/pagos?userId={userId}",
    helper: "Da de alta un nuevo pago de sueldo.",
    getInitialState: () => ({
      userId: "1",
      empleadoId: "1",
      comedorId: "1",
      periodoInicio: "2026-03-01",
      periodoFin: "2026-03-31",
      contrato: "MENSUAL",
      funcionEmpleado: "Cocinero",
      fechaPago: new Date().toISOString().slice(0, 10),
      montoTotal: "150000.00",
      observaciones: "",
    }),
    getVisibleFields: () => [
      { name: "userId", label: "Usuario ID", type: "number" },
      { name: "empleadoId", label: "Empleado ID", type: "number" },
      { name: "comedorId", label: "Comedor ID", type: "number" },
      { name: "periodoInicio", label: "Período inicio", type: "date" },
      { name: "periodoFin", label: "Período fin", type: "date" },
      { name: "contrato", label: "Contrato", type: "select", options: CONTRATO_OPTIONS },
      { name: "funcionEmpleado", label: "Función", type: "text" },
      { name: "fechaPago", label: "Fecha de pago", type: "date" },
      { name: "montoTotal", label: "Monto total", type: "number" },
      { name: "observaciones", label: "Observaciones", type: "textarea" },
    ],
    buildPath: () => "/api/sueldos/pagos",
    buildPayload: (f) => ({
      query: { userId: f.userId },
      body: {
        empleadoId: Number(f.empleadoId),
        comedorId: Number(f.comedorId),
        periodoInicio: f.periodoInicio,
        periodoFin: f.periodoFin,
        contrato: f.contrato,
        funcionEmpleado: f.funcionEmpleado,
        fechaPago: f.fechaPago,
        montoTotal: Number(f.montoTotal),
        observaciones: f.observaciones || null,
      },
    }),
  },
  {
    id: "sueldos-get",
    module: "Sueldos",
    method: "GET",
    name: "Ver pago de sueldo",
    pathLabel: "/api/sueldos/pagos/{id}",
    helper: "Muestra detalle de un pago de sueldo.",
    getInitialState: () => ({ pagoId: "1" }),
    getVisibleFields: () => [{ name: "pagoId", label: "Pago ID", type: "number" }],
    buildPath: (f) => `/api/sueldos/pagos/${f.pagoId}`,
    buildPayload: () => ({ query: {}, body: null }),
  },
  {
    id: "sueldos-agregar-movimiento",
    module: "Sueldos",
    method: "POST",
    name: "Agregar movimiento parcial",
    pathLabel: "/api/sueldos/pagos/{id}/movimientos?userId={userId}",
    helper: "Registra un pago parcial dentro del pago de sueldo.",
    getInitialState: () => ({
      pagoId: "1",
      userId: "1",
      medioPago: "TRANSFERENCIABANCARIA",
      montoParcial: "50000.00",
      numeroOperacion: "TRX-001",
    }),
    getVisibleFields: () => [
      { name: "pagoId", label: "Pago ID", type: "number" },
      { name: "userId", label: "Usuario ID", type: "number" },
      { name: "medioPago", label: "Medio de pago", type: "select", options: MEDIO_PAGO_OPTIONS },
      { name: "montoParcial", label: "Monto parcial", type: "number" },
      { name: "numeroOperacion", label: "Número de operación", type: "text" },
    ],
    buildPath: (f) => `/api/sueldos/pagos/${f.pagoId}/movimientos`,
    buildPayload: (f) => ({
      query: { userId: f.userId },
      body: {
        medioPago: f.medioPago,
        montoParcial: Number(f.montoParcial),
        numeroOperacion: f.numeroOperacion,
      },
    }),
  },
  {
    id: "sueldos-reemplazar-movimiento",
    module: "Sueldos",
    method: "PUT",
    name: "Reemplazar movimiento parcial",
    pathLabel: "/api/sueldos/pagos/{id}/movimientos/reemplazar?userId={userId}",
    helper: "Anula un movimiento parcial anterior y crea uno nuevo.",
    getInitialState: () => ({
      pagoId: "1",
      userId: "1",
      movimientoIdOriginal: "1",
      nuevoMedioPago: "TRANSFERENCIABANCARIA",
      nuevoMontoParcial: "50000.00",
      nuevoNumeroOperacion: "TRX-002",
      motivo: "Corrección de datos",
    }),
    getVisibleFields: () => [
      { name: "pagoId", label: "Pago ID", type: "number" },
      { name: "userId", label: "Usuario ID", type: "number" },
      { name: "movimientoIdOriginal", label: "Movimiento original ID", type: "number" },
      { name: "nuevoMedioPago", label: "Nuevo medio de pago", type: "select", options: MEDIO_PAGO_OPTIONS },
      { name: "nuevoMontoParcial", label: "Nuevo monto parcial", type: "number" },
      { name: "nuevoNumeroOperacion", label: "Nuevo número operación", type: "text" },
      { name: "motivo", label: "Motivo", type: "text" },
    ],
    buildPath: (f) => `/api/sueldos/pagos/${f.pagoId}/movimientos/reemplazar`,
    buildPayload: (f) => ({
      query: { userId: f.userId },
      body: {
        movimientoIdOriginal: Number(f.movimientoIdOriginal),
        nuevoMedioPago: f.nuevoMedioPago,
        nuevoMontoParcial: Number(f.nuevoMontoParcial),
        nuevoNumeroOperacion: f.nuevoNumeroOperacion,
        motivo: f.motivo,
      },
    }),
  },
  {
    id: "sueldos-anular-movimiento",
    module: "Sueldos",
    method: "PUT",
    name: "Anular movimiento parcial",
    pathLabel: "/api/sueldos/pagos/{id}/movimientos/anular?userId={userId}",
    helper: "Anula un movimiento parcial específico.",
    getInitialState: () => ({
      pagoId: "1",
      userId: "1",
      movimientoId: "1",
      motivo: "Movimiento inválido",
    }),
    getVisibleFields: () => [
      { name: "pagoId", label: "Pago ID", type: "number" },
      { name: "userId", label: "Usuario ID", type: "number" },
      { name: "movimientoId", label: "Movimiento ID", type: "number" },
      { name: "motivo", label: "Motivo", type: "text" },
    ],
    buildPath: (f) => `/api/sueldos/pagos/${f.pagoId}/movimientos/anular`,
    buildPayload: (f) => ({
      query: { userId: f.userId },
      body: {
        movimientoId: Number(f.movimientoId),
        motivo: f.motivo,
      },
    }),
  },
  {
    id: "sueldos-anular",
    module: "Sueldos",
    method: "PUT",
    name: "Anular pago de sueldo",
    pathLabel: "/api/sueldos/pagos/{id}/anular?userId={userId}",
    helper: "Anula un pago completo de sueldo.",
    getInitialState: () => ({ pagoId: "1", userId: "1", motivo: "Corrección administrativa" }),
    getVisibleFields: () => [
      { name: "pagoId", label: "Pago ID", type: "number" },
      { name: "userId", label: "Usuario ID", type: "number" },
      { name: "motivo", label: "Motivo", type: "text" },
    ],
    buildPath: (f) => `/api/sueldos/pagos/${f.pagoId}/anular`,
    buildPayload: (f) => ({
      query: { userId: f.userId },
      body: { motivo: f.motivo },
    }),
  },
  {
    id: "facturas-crear",
    module: "Facturas",
    method: "POST",
    name: "Crear factura proveedor",
    pathLabel: "/api/facturas-proveedor",
    helper: "Crea una factura nueva para proveedor.",
    getInitialState: () => ({
      comedorId: "1",
      fechaFactura: new Date().toISOString().slice(0, 10),
      numeroFactura: "A-0001-00000001",
      proveedorId: "1",
      monto: "25000.00",
      medioPago: "TRANSFERENCIABANCARIA",
      observaciones: "",
    }),
    getVisibleFields: () => [
      { name: "comedorId", label: "Comedor ID", type: "number" },
      { name: "fechaFactura", label: "Fecha factura", type: "date" },
      { name: "numeroFactura", label: "Número factura", type: "text" },
      { name: "proveedorId", label: "Proveedor ID", type: "number" },
      { name: "monto", label: "Monto", type: "number" },
      { name: "medioPago", label: "Medio de pago", type: "select", options: MEDIO_PAGO_OPTIONS },
      { name: "observaciones", label: "Observaciones", type: "textarea" },
    ],
    buildPath: () => "/api/facturas-proveedor",
    buildPayload: (f) => ({
      query: {},
      body: {
        comedorId: Number(f.comedorId),
        fechaFactura: f.fechaFactura,
        numeroFactura: f.numeroFactura,
        proveedorId: Number(f.proveedorId),
        monto: Number(f.monto),
        medioPago: f.medioPago,
        observaciones: f.observaciones || null,
      },
    }),
  },
  {
    id: "facturas-get",
    module: "Facturas",
    method: "GET",
    name: "Ver factura proveedor",
    pathLabel: "/api/facturas-proveedor/{id}",
    helper: "Muestra una factura por ID.",
    getInitialState: () => ({ facturaId: "1" }),
    getVisibleFields: () => [{ name: "facturaId", label: "Factura ID", type: "number" }],
    buildPath: (f) => `/api/facturas-proveedor/${f.facturaId}`,
    buildPayload: () => ({ query: {}, body: null }),
  },
  {
    id: "facturas-actualizar",
    module: "Facturas",
    method: "PATCH",
    name: "Actualizar factura",
    pathLabel: "/api/facturas-proveedor/{id}",
    helper: "Modifica datos de la factura antes de su aprobación.",
    getInitialState: () => ({
      facturaId: "1",
      comedorId: "",
      fechaFactura: "",
      numeroFactura: "",
      proveedorId: "",
      monto: "",
      medioPago: "",
      observaciones: "",
    }),
    getVisibleFields: () => [
      { name: "facturaId", label: "Factura ID", type: "number" },
      { name: "comedorId", label: "Comedor ID (opcional)", type: "number" },
      { name: "fechaFactura", label: "Fecha factura (opcional)", type: "date" },
      { name: "numeroFactura", label: "Número factura (opcional)", type: "text" },
      { name: "proveedorId", label: "Proveedor ID (opcional)", type: "number" },
      { name: "monto", label: "Monto (opcional)", type: "number" },
      { name: "medioPago", label: "Medio de pago (opcional)", type: "select", options: MEDIO_PAGO_OPTIONS },
      { name: "observaciones", label: "Observaciones (opcional)", type: "textarea" },
    ],
    buildPath: (f) => `/api/facturas-proveedor/${f.facturaId}`,
    buildPayload: (f) => ({
      query: {},
      body: compactObject({
        comedorId: f.comedorId ? Number(f.comedorId) : undefined,
        fechaFactura: f.fechaFactura || undefined,
        numeroFactura: f.numeroFactura || undefined,
        proveedorId: f.proveedorId ? Number(f.proveedorId) : undefined,
        monto: f.monto ? Number(f.monto) : undefined,
        medioPago: f.medioPago || undefined,
        observaciones: f.observaciones || undefined,
      }),
    }),
  },
  {
    id: "facturas-aprobar",
    module: "Facturas",
    method: "POST",
    name: "Aprobar factura",
    pathLabel: "/api/facturas-proveedor/{id}/approve",
    helper: "Pasa la factura al estado aprobado.",
    getInitialState: () => ({
      facturaId: "1",
      fechaEmision: new Date().toISOString().slice(0, 10),
      fechaPagoProvisoria: new Date().toISOString().slice(0, 10),
      bancoPagadorId: "1",
      observaciones: "Aprobada desde dashboard",
    }),
    getVisibleFields: () => [
      { name: "facturaId", label: "Factura ID", type: "number" },
      { name: "fechaEmision", label: "Fecha emisión", type: "date" },
      { name: "fechaPagoProvisoria", label: "Fecha pago provisoria", type: "date" },
      { name: "bancoPagadorId", label: "Banco pagador ID", type: "number" },
      { name: "observaciones", label: "Observaciones", type: "textarea" },
    ],
    buildPath: (f) => `/api/facturas-proveedor/${f.facturaId}/approve`,
    buildPayload: (f) => ({
      query: {},
      body: {
        fechaEmision: f.fechaEmision,
        fechaPagoProvisoria: f.fechaPagoProvisoria || null,
        bancoPagadorId: f.bancoPagadorId ? Number(f.bancoPagadorId) : null,
        observaciones: f.observaciones || null,
      },
    }),
  },
  {
    id: "facturas-pagar",
    module: "Facturas",
    method: "POST",
    name: "Pagar factura",
    pathLabel: "/api/facturas-proveedor/{id}/pay",
    helper: "Marca la factura como pagada.",
    getInitialState: () => ({
      facturaId: "1",
      fechaPago: new Date().toISOString().slice(0, 10),
      observaciones: "Pago registrado",
    }),
    getVisibleFields: () => [
      { name: "facturaId", label: "Factura ID", type: "number" },
      { name: "fechaPago", label: "Fecha pago", type: "date" },
      { name: "observaciones", label: "Observaciones", type: "textarea" },
    ],
    buildPath: (f) => `/api/facturas-proveedor/${f.facturaId}/pay`,
    buildPayload: (f) => ({
      query: {},
      body: { fechaPago: f.fechaPago || null, observaciones: f.observaciones || null },
    }),
  },
  {
    id: "facturas-cancelar",
    module: "Facturas",
    method: "POST",
    name: "Cancelar factura",
    pathLabel: "/api/facturas-proveedor/{id}/cancel?observaciones={texto}",
    helper: "Cancela una factura y deja observación opcional.",
    getInitialState: () => ({
      facturaId: "1",
      observaciones: "Cancelada desde dashboard",
    }),
    getVisibleFields: () => [
      { name: "facturaId", label: "Factura ID", type: "number" },
      { name: "observaciones", label: "Observaciones (opcional)", type: "textarea" },
    ],
    buildPath: (f) => `/api/facturas-proveedor/${f.facturaId}/cancel`,
    buildPayload: (f) => ({
      query: {
        observaciones: f.observaciones || undefined,
      },
      body: null,
    }),
  },
];

function App() {
  const [baseUrl, setBaseUrl] = useState(getDefaultBaseUrl);
  const [moduleTab, setModuleTab] = useState("Caja");
  const [seedCopyStatus, setSeedCopyStatus] = useState("");

  const modules = useMemo(() => ["Caja", "Sueldos", "Facturas"], []);
  const filteredActions = useMemo(
    () => actions.filter((a) => a.module === moduleTab),
    [moduleTab]
  );
  
  const onCopySeedSql = async () => {
    try {
      await copyToClipboard(SEED_SQL);
      setSeedCopyStatus("SQL de seed copiado al portapapeles.");
    } catch (err) {
      setSeedCopyStatus("No se pudo copiar automáticamente.");
    }
  };

  return (
    <div className="container">
      <header className="header">
        <h1 className="title">Panel de Demostración - Gestión Comedores</h1>
        <p className="subtitle">
          Este panel permite probar acciones del sistema con un lenguaje claro para usuarios no técnicos.
        </p>

        <div className="seed-row">
          <button className="btn secondary" type="button" onClick={onCopySeedSql}>
            Copiar SQL de seed
          </button>
          {seedCopyStatus && <span className="seed-copy-status">{seedCopyStatus}</span>}
        </div>

        <div className="base-url-row">
          <label htmlFor="base-url">URL del sistema</label>
          <input
            id="base-url"
            type="text"
            value={baseUrl}
            onChange={(e) => setBaseUrl(e.target.value)}
            placeholder="http://localhost:8080"
          />
        </div>
      </header>

      <div className="tabs">
        {modules.map((m) => (
          <button
            key={m}
            className={`tab-button ${m === moduleTab ? "active" : ""}`}
            onClick={() => setModuleTab(m)}
            type="button"
          >
            {m}
          </button>
        ))}
      </div>

      <section className="card-grid">
        {filteredActions.map((action) => (
          <EndpointCard key={action.id} action={action} baseUrl={baseUrl} />
        ))}
      </section>
    </div>
  );
}

const root = ReactDOM.createRoot(document.getElementById("root"));
root.render(<App />);
