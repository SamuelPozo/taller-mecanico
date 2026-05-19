import React, { useEffect, useState } from 'react';
import {
  View, Text, StyleSheet, FlatList,
  ActivityIndicator, RefreshControl
} from 'react-native';
import AsyncStorage from '@react-native-async-storage/async-storage';
import api from '../../api/api';

const DIA_COLORS: Record<string, string> = {
  LUNES: '#3498DB',
  MARTES: '#3498DB',
  MIERCOLES: '#3498DB',
  JUEVES: '#3498DB',
  VIERNES: '#3498DB',
  SABADO: '#E94560',
  DOMINGO: '#E94560',
};

export default function MecanicoHorariosScreen() {
  const [horarios, setHorarios] = useState<any[]>([]);
  const [loading, setLoading] = useState(true);
  const [refreshing, setRefreshing] = useState(false);

  useEffect(() => { loadHorarios(); }, []);

  const loadHorarios = async () => {
    try {
      const userStr = await AsyncStorage.getItem('user');
      const user = JSON.parse(userStr!);
      const response = await api.get(`/mecanico/horarios/${user.mecanicoId}`);
      setHorarios(response.data);
    } catch (error) {
      console.error(error);
    } finally {
      setLoading(false);
      setRefreshing(false);
    }
  };

  const renderHorario = ({ item }: { item: any }) => (
    <View style={styles.card}>
      <View style={[styles.diaBadge, { backgroundColor: DIA_COLORS[item.diaSemana] || '#555575' }]}>
        <Text style={styles.diaText}>{item.diaSemana}</Text>
      </View>
      <View style={styles.horas}>
        <Text style={styles.hora}>🕗 Entrada: {item.horaEntrada}</Text>
        <Text style={styles.hora}>🕔 Salida: {item.horaSalida}</Text>
      </View>
    </View>
  );

  if (loading) {
    return <View style={styles.center}><ActivityIndicator size="large" color="#E94560" /></View>;
  }

  return (
    <View style={styles.container}>
      <FlatList
        data={horarios}
        keyExtractor={(item) => item.id.toString()}
        renderItem={renderHorario}
        contentContainerStyle={styles.list}
        refreshControl={<RefreshControl refreshing={refreshing} onRefresh={() => { setRefreshing(true); loadHorarios(); }} tintColor="#E94560" />}
        ListEmptyComponent={<Text style={styles.empty}>No hay horarios registrados.</Text>}
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
    flexDirection: 'row',
    alignItems: 'center',
    gap: 16,
  },
  diaBadge: {
    borderRadius: 8,
    paddingHorizontal: 12,
    paddingVertical: 8,
    minWidth: 90,
    alignItems: 'center',
  },
  diaText: { color: 'white', fontWeight: 'bold', fontSize: 12 },
  horas: { flex: 1 },
  hora: { color: '#EAEAEA', fontSize: 14, marginBottom: 4 },
  empty: { color: '#A0A0B0', textAlign: 'center', marginTop: 40 },
});