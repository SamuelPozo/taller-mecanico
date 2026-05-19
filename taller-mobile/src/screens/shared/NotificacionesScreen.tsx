import React, { useEffect, useState } from 'react';
import {
  View, Text, StyleSheet, FlatList,
  ActivityIndicator, RefreshControl, TouchableOpacity
} from 'react-native';
import api from '../../api/api';
import { formatFechaHora } from '../../utils/fecha';

const TIPO_COLORS: Record<string, string> = {
  ESTADO: '#3498DB',
  AUSENCIA: '#F39C12',
  CHAT: '#9B59B6',
  GENERAL: '#555575',
};

const TIPO_ICONS: Record<string, string> = {
  ESTADO: '🔄',
  AUSENCIA: '🏖️',
  CHAT: '💬',
  GENERAL: '🔔',
};

export default function NotificacionesScreen() {
  const [notificaciones, setNotificaciones] = useState<any[]>([]);
  const [loading, setLoading] = useState(true);
  const [refreshing, setRefreshing] = useState(false);

  useEffect(() => { loadNotificaciones(); }, []);

  const loadNotificaciones = async () => {
    try {
      const response = await api.get('/notificaciones');
      setNotificaciones(response.data);
    } catch (error) {
      console.error(error);
    } finally {
      setLoading(false);
      setRefreshing(false);
    }
  };

  const marcarLeida = async (id: number) => {
    try {
      await api.patch(`/notificaciones/${id}/leer`);
      setNotificaciones(prev =>
        prev.map(n => n.id === id ? { ...n, leida: true } : n)
      );
    } catch (error) {
      console.error(error);
    }
  };

  const marcarTodasLeidas = async () => {
    try {
      await api.patch('/notificaciones/leer-todas');
      setNotificaciones(prev => prev.map(n => ({ ...n, leida: true })));
    } catch (error) {
      console.error(error);
    }
  };

  const renderNotificacion = ({ item }: { item: any }) => (
    <TouchableOpacity
      style={[styles.card, item.leida && styles.cardLeida]}
      onPress={() => !item.leida && marcarLeida(item.id)}
    >
      <View style={styles.cardHeader}>
        <View style={styles.iconContainer}>
          <Text style={styles.icon}>{TIPO_ICONS[item.tipo] ?? '🔔'}</Text>
          {!item.leida && <View style={styles.unreadDot} />}
        </View>
        <View style={styles.cardContent}>
          <View style={styles.titleRow}>
            <Text style={[styles.titulo, item.leida && styles.tituloLeido]}>
              {item.titulo}
            </Text>
            <View style={[styles.tipoBadge, { backgroundColor: TIPO_COLORS[item.tipo] }]}>
              <Text style={styles.tipoBadgeText}>{item.tipo}</Text>
            </View>
          </View>
          <Text style={styles.mensaje}>{item.mensaje}</Text>
          <Text style={styles.fecha}>{formatFechaHora(item.fecha)}</Text>
        </View>
      </View>
      {!item.leida && (
        <Text style={styles.marcarTexto}>Toca para marcar como leída</Text>
      )}
    </TouchableOpacity>
  );

  if (loading) {
    return <View style={styles.center}><ActivityIndicator size="large" color="#E94560" /></View>;
  }

  const noLeidas = notificaciones.filter(n => !n.leida).length;

  return (
    <View style={styles.container}>
      {noLeidas > 0 && (
        <TouchableOpacity style={styles.marcarTodasBtn} onPress={marcarTodasLeidas}>
          <Text style={styles.marcarTodasText}>Marcar todas como leídas ({noLeidas})</Text>
        </TouchableOpacity>
      )}
      <FlatList
        data={notificaciones}
        keyExtractor={(item) => item.id.toString()}
        renderItem={renderNotificacion}
        contentContainerStyle={styles.list}
        refreshControl={<RefreshControl refreshing={refreshing} onRefresh={() => { setRefreshing(true); loadNotificaciones(); }} tintColor="#E94560" />}
        ListEmptyComponent={<Text style={styles.empty}>No tienes notificaciones.</Text>}
      />
    </View>
  );
}

const styles = StyleSheet.create({
  container: { flex: 1, backgroundColor: '#1A1A2E' },
  center: { flex: 1, backgroundColor: '#1A1A2E', justifyContent: 'center', alignItems: 'center' },
  list: { padding: 16, gap: 10 },
  marcarTodasBtn: { backgroundColor: '#1E1E3A', padding: 12, alignItems: 'center', borderBottomWidth: 1, borderBottomColor: '#2A2A4A' },
  marcarTodasText: { color: '#E94560', fontSize: 13, fontWeight: 'bold' },
  card: { backgroundColor: '#1E1E3A', borderRadius: 12, padding: 14, borderLeftWidth: 4, borderLeftColor: '#E94560' },
  cardLeida: { borderLeftColor: '#2A2A4A', opacity: 0.7 },
  cardHeader: { flexDirection: 'row', gap: 12 },
  iconContainer: { position: 'relative' },
  icon: { fontSize: 24 },
  unreadDot: { position: 'absolute', top: 0, right: -2, width: 8, height: 8, borderRadius: 4, backgroundColor: '#E94560' },
  cardContent: { flex: 1 },
  titleRow: { flexDirection: 'row', justifyContent: 'space-between', alignItems: 'center', marginBottom: 4 },
  titulo: { color: '#EAEAEA', fontSize: 14, fontWeight: 'bold', flex: 1, marginRight: 8 },
  tituloLeido: { color: '#A0A0B0' },
  tipoBadge: { borderRadius: 8, paddingHorizontal: 6, paddingVertical: 2 },
  tipoBadgeText: { color: 'white', fontSize: 9, fontWeight: 'bold' },
  mensaje: { color: '#A0A0B0', fontSize: 13, marginBottom: 6 },
  fecha: { color: '#555575', fontSize: 11 },
  marcarTexto: { color: '#555575', fontSize: 11, marginTop: 6, textAlign: 'right' },
  empty: { color: '#A0A0B0', textAlign: 'center', marginTop: 40 },
});