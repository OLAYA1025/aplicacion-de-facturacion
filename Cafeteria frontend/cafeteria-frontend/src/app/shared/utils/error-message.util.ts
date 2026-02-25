export function obtenerMensajeError(error: unknown, fallback: string): string {
  if (typeof error === 'string' && error.trim().length > 0) {
    return error;
  }

  const errorObject = error as
    | {
        message?: unknown;
        error?: unknown;
      }
    | undefined;

  const payload = errorObject?.error;

  if (typeof payload === 'string' && payload.trim().length > 0) {
    return payload;
  }

  if (payload && typeof payload === 'object') {
    const payloadObject = payload as {
      message?: unknown;
      mensaje?: unknown;
      error?: unknown;
      detail?: unknown;
      title?: unknown;
    };

    if (typeof payloadObject.message === 'string' && payloadObject.message.trim().length > 0) {
      return payloadObject.message;
    }

    if (typeof payloadObject.mensaje === 'string' && payloadObject.mensaje.trim().length > 0) {
      return payloadObject.mensaje;
    }

    if (typeof payloadObject.error === 'string' && payloadObject.error.trim().length > 0) {
      return payloadObject.error;
    }

    if (typeof payloadObject.detail === 'string' && payloadObject.detail.trim().length > 0) {
      return payloadObject.detail;
    }

    if (typeof payloadObject.title === 'string' && payloadObject.title.trim().length > 0) {
      return payloadObject.title;
    }
  }

  if (typeof errorObject?.message === 'string' && errorObject.message.trim().length > 0) {
    return errorObject.message;
  }

  return fallback;
}
