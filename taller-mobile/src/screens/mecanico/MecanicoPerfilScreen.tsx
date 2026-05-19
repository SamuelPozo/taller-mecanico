import React, { useEffect, useState } from 'react';
import {
  View, Text, StyleSheet, TouchableOpacity, Alert,
  ActivityIndicator, Image
} from 'react-native';
import AsyncStorage from '@react-native-async-storage/async-storage';
import * as ImagePicker from 'expo-image-picker';
import { clearAuth } from '../../store/authStore';
import api from '../../api/api';

export default function MecanicoPerfilScreen({ navigation }: any) {
  const [user, setUser] = useState<any>(null);
  const [loading, setLoading] = useState(true);
  const [uploadingFoto, setUploadingFoto] = useState(false);

  useEffect(() => { loadUser(); }, []);

  const loadUser = async () => {
    try {
      const userStr = await AsyncStorage.getItem('user');
      if (userStr) setUser(JSON.parse(userStr));
    } finally {
      setLoading(false);
    }
  };

  const handleSeleccionarFoto = async () => {
    const { status } = await ImagePicker.requestMediaLibraryPermissionsAsync();
    if (status !== 'granted') {
      Alert.alert('Permiso denegado', 'Necesitamos acceso a tu galería para cambiar la foto.');
      return;
    }

    const result = await ImagePicker.launchImageLibraryAsync({
      mediaTypes: ImagePicker.MediaTypeOptions.Images,
      allowsEditing: true,
      aspect: [1, 1],
      quality: 0.5,
      base64: true,
    });

    if (!result.canceled && result.assets[0].base64) {
      setUploadingFoto(true);
      try {
        const base64 = `data:image/jpeg;base64,${result.assets[0].base64}`;
        await api.patch('/auth/foto-perfil', { fotoPerfil: base64 });
        const updatedUser = { ...user, fotoPerfil: base64 };
        setUser(updatedUser);
        await AsyncStorage.setItem('user', JSON.stringify(updatedUser));
        Alert.alert('Éxito', 'Foto de perfil actualizada correctamente.');
      } catch (error: any) {
        Alert.alert('Error', error.message || 'No se pudo actualizar la foto.');
      } finally {
        setUploadingFoto(false);
      }
    }
  };

  const handleEliminarFoto = async () => {
    Alert.alert('Eliminar foto', '¿Estás seguro de que quieres eliminar tu foto de perfil?', [
      { text: 'Cancelar', style: 'cancel' },
      {
        text: 'Eliminar',
        style: 'destructive',
        onPress: async () => {
          setUploadingFoto(true);
          try {
            await api.patch('/auth/foto-perfil', { fotoPerfil: null });
            const updatedUser = { ...user, fotoPerfil: null };
            setUser(updatedUser);
            await AsyncStorage.setItem('user', JSON.stringify(updatedUser));
          } catch (error: any) {
            Alert.alert('Error', error.message || 'No se pudo eliminar la foto.');
          } finally {
            setUploadingFoto(false);
          }
        }
      }
    ]);
  };

  const handleLogout = async () => {
    Alert.alert('Cerrar sesión', '¿Estás seguro?', [
      { text: 'Cancelar', style: 'cancel' },
      {
        text: 'Cerrar sesión',
        style: 'destructive',
        onPress: async () => {
          await clearAuth();
          navigation.replace('Login');
        },
      },
    ]);
  };

  if (loading) {
    return <View style={styles.center}><ActivityIndicator size="large" color="#E94560" /></View>;
  }

  return (
    <View style={styles.container}>
      <TouchableOpacity style={styles.avatarContainer} onPress={handleSeleccionarFoto} disabled={uploadingFoto}>
        {user?.fotoPerfil ? (
          <Image source={{ uri: user.fotoPerfil }} style={styles.avatarImage} />
        ) : (
          <View style={styles.avatar}>
            <Text style={styles.avatarText}>👨‍🔧</Text>
          </View>
        )}
        {uploadingFoto ? (
          <View style={styles.avatarOverlay}>
            <ActivityIndicator color="white" />
          </View>
        ) : (
          <View style={styles.avatarOverlay}>
            <Text style={styles.avatarOverlayText}>📷</Text>
          </View>
        )}
      </TouchableOpacity>

      {user?.fotoPerfil && (
        <TouchableOpacity onPress={handleEliminarFoto} style={styles.eliminarFotoBtn}>
          <Text style={styles.eliminarFotoText}>Eliminar foto</Text>
        </TouchableOpacity>
      )}

      <Text style={styles.nombre}>{user?.nombre} {user?.apellidos}</Text>
      <Text style={styles.rol}>Mecánico</Text>

      <View style={styles.card}>
        <View style={styles.row}>
          <Text style={styles.label}>Email</Text>
          <Text style={styles.value}>{user?.email}</Text>
        </View>
        <View style={styles.divider} />
        <View style={styles.row}>
          <Text style={styles.label}>Rol</Text>
          <Text style={styles.value}>{user?.rol}</Text>
        </View>
      </View>

      <TouchableOpacity style={styles.button} onPress={handleLogout}>
        <Text style={styles.buttonText}>Cerrar Sesión</Text>
      </TouchableOpacity>
    </View>
  );
}

const styles = StyleSheet.create({
  container: { flex: 1, backgroundColor: '#1A1A2E', alignItems: 'center', padding: 24 },
  center: { flex: 1, backgroundColor: '#1A1A2E', justifyContent: 'center', alignItems: 'center' },
  avatarContainer: { marginTop: 32, marginBottom: 16, position: 'relative' },
  avatar: {
    width: 90, height: 90, borderRadius: 45,
    backgroundColor: '#1E1E3A', justifyContent: 'center',
    alignItems: 'center', borderWidth: 2, borderColor: '#E94560',
  },
  avatarImage: {
    width: 90, height: 90, borderRadius: 45,
    borderWidth: 2, borderColor: '#E94560',
  },
  avatarText: { fontSize: 40 },
  avatarOverlay: {
    position: 'absolute', bottom: 0, right: 0,
    backgroundColor: '#E94560', borderRadius: 12,
    width: 24, height: 24, justifyContent: 'center', alignItems: 'center',
  },
  avatarOverlayText: { fontSize: 12 },
  nombre: { color: '#EAEAEA', fontSize: 22, fontWeight: 'bold', marginBottom: 4 },
  rol: { color: '#E94560', fontSize: 13, fontWeight: 'bold', marginBottom: 32 },
  card: {
    backgroundColor: '#1E1E3A', borderRadius: 12,
    padding: 16, width: '100%', marginBottom: 24,
  },
  row: { flexDirection: 'row', justifyContent: 'space-between', paddingVertical: 8 },
  divider: { height: 1, backgroundColor: '#2A2A4A' },
  label: { color: '#A0A0B0', fontSize: 14 },
  value: { color: '#EAEAEA', fontSize: 14, fontWeight: 'bold' },
  button: {
    backgroundColor: '#E94560', borderRadius: 8,
    padding: 14, paddingHorizontal: 48,
  },
  buttonText: { color: 'white', fontWeight: 'bold', fontSize: 14 },
  eliminarFotoBtn: { marginTop: 8, marginBottom: 8 },
  eliminarFotoText: { color: '#E94560', fontSize: 12, textDecorationLine: 'underline' },
});