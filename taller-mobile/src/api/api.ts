import axios from 'axios';
import AsyncStorage from '@react-native-async-storage/async-storage';

const BASE_URL = 'http://10.0.2.2:8080/api';

const api = axios.create({
  baseURL: BASE_URL,
  timeout: 10000,
  headers: {
    'Content-Type': 'application/json',
  },
});

api.interceptors.request.use(
  async (config) => {
    const token = await AsyncStorage.getItem('token');
    if (token) {
      config.headers.Authorization = `Bearer ${token}`;
    }
    return config;
  },
  (error) => Promise.reject(error)
);

api.interceptors.response.use(
  (response) => response,
  async (error) => {
    if (error.response?.status === 401) {
      await AsyncStorage.removeItem('token');
      await AsyncStorage.removeItem('user');
    }

    // Extraer mensaje legible del backend si existe
    const backendMessage = error.response?.data?.message;
    if (backendMessage) {
      error.message = backendMessage;
    } else {
      // Mensajes genéricos por código de estado
      switch (error.response?.status) {
        case 400:
          error.message = 'Datos incorrectos. Comprueba los campos introducidos.';
          break;
        case 401:
          error.message = 'Sesión expirada. Por favor, inicia sesión de nuevo.';
          break;
        case 403:
          error.message = 'No tienes permiso para realizar esta acción.';
          break;
        case 404:
          error.message = 'El recurso solicitado no existe.';
          break;
        case 409:
          error.message = 'Ya existe un registro con esos datos.';
          break;
        case 500:
          error.message = 'Error del servidor. Inténtalo de nuevo más tarde.';
          break;
        default:
          if (!error.response) {
            error.message = 'No se puede conectar con el servidor. Comprueba tu conexión.';
          } else {
            error.message = 'Ha ocurrido un error inesperado. Inténtalo de nuevo.';
          }
      }
    }

    return Promise.reject(error);
  }
);

export default api;