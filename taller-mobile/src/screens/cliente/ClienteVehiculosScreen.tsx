import React, { useEffect, useState } from 'react';
import {
  View, Text, StyleSheet, FlatList,
  ActivityIndicator, RefreshControl
} from 'react-native';
import AsyncStorage from '@react-native-async-storage/async-storage';
import api from '../../api/api';

export default function ClienteVehiculosScreen() {
  const [vehiculos, setVehiculos] = useState<any[]>([]);
  const [loading, setLoading] = useState(true);
  const [refreshing, setRefreshing] = useState(false);

  useEffect(() => { loadVehiculos(); }, []);

  const loadVehiculos = async () => {
    try {
      const userStr = await AsyncStorage.getItem('user');
      const user = JSON.parse(userStr!);
      const response = await api.get(`/cliente/vehiculos/${user.clienteId}`);
      setVehiculos(response.data);
    } catch (error) {
      console.error(error);
    } finally {
      setLoading(false);
      setRefreshing(false);
    }
  };

  const renderVehiculo = ({ item }: { item: any }) => (
    <View style={styles.card}>
      <Text style={styles.matricula}>🚗 {item.matricula}</Text>
      <Text style={styles.nombre}>{item.marca} {item.modelo}</Text>
      <View style={styles.row}>
        <Text style={styles.detalle}>📅 {item.anio}</Text>
        {item.color && <Text style={styles.detalle}>🎨 {item.color}</Text>}
      </View>
    </View>
  );

  if (loading) {
    return <View style={styles.center}><ActivityIndicator size="large" color="#E94560" /></View>;
  }

  return (
    <View style={styles.container}>
      <FlatList
        data={vehiculos}
        keyExtractor={(item) => item.id.toString()}
        renderItem={renderVehiculo}
        contentContainerStyle={styles.list}
        refreshControl={<RefreshControl refreshing={refreshing} onRefresh={() => { setRefreshing(true); loadVehiculos(); }} tintColor="#E94560" />}
        ListEmptyComponent={<Text style={styles.empty}>No tienes vehículos registrados.</Text>}
      />
    </View>
  );
}

const styles = StyleSheet.create({
  container: { flex: 1, backgroundColor: '#1A1A2E' },
  center: { flex: 1, backgroundColor: '#1A1A2E', justifyContent: 'center', alignItems: 'center' },
  list: { padding: 16, gap: 12 },
  card: {
    backgroundColor: '#1E1E3A',
    borderRadius: 12,
    padding: 16,
    borderLeftWidth: 4,
    borderLeftColor: '#3498DB',
  },
  matricula: { color: '#A0A0B0', fontSize: 12, fontWeight: 'bold', marginBottom: 4 },
  nombre: { color: '#EAEAEA', fontSize: 18, fontWeight: 'bold', marginBottom: 8 },
  row: { flexDirection: 'row', gap: 16 },
  detalle: { color: '#A0A0B0', fontSize: 13 },
  empty: { color: '#A0A0B0', textAlign: 'center', marginTop: 40 },
});