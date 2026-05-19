import AsyncStorage from '@react-native-async-storage/async-storage';
import { AuthResponse } from '../types';

export const saveAuth = async (data: AuthResponse): Promise<void> => {
  await AsyncStorage.setItem('token', data.token);
  await AsyncStorage.setItem('user', JSON.stringify({
    id: data.id,
    nombre: data.nombre,
    apellidos: data.apellidos,
    email: data.email,
    rol: data.rol,
    mecanicoId: data.mecanicoId,
    clienteId: data.clienteId,
  }));
};

export const getToken = async (): Promise<string | null> => {
  return await AsyncStorage.getItem('token');
};

export const getUser = async (): Promise<AuthResponse | null> => {
  const user = await AsyncStorage.getItem('user');
  return user ? JSON.parse(user) : null;
};

export const clearAuth = async (): Promise<void> => {
  await AsyncStorage.removeItem('token');
  await AsyncStorage.removeItem('user');
};

export const isAuthenticated = async (): Promise<boolean> => {
  const token = await AsyncStorage.getItem('token');
  return token !== null;
};