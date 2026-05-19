import React, { useEffect, useState } from 'react';
import {
  View, Text, StyleSheet, FlatList, ActivityIndicator,
  TouchableOpacity, RefreshControl, Modal, ScrollView
} from 'react-native';
import AsyncStorage from '@react-native-async-storage/async-storage';
import api from '../../api/api';
import { formatFecha, formatFechaHora } from '../../utils/fecha';

const ESTADO_COLORS: Record<string, string> = {
  PENDIENTE: '#F39C12',
  EN_PROCESO: '#3498DB',
  DIAGNOSTICADO: '#9B59B6',
  REPARADO: '#1ABC9C',
  LISTO: '#2ECC71',
  ENTREGADO: '#555575',
};

const ESTADO_LABELS: Record<string, string> = {
  PENDIENTE: 'Pendiente de revisión',
  EN_PROCESO: 'En proceso de reparación',
  DIAGNOSTICADO: 'Diagnóstico realizado',
  REPARADO: 'Reparación completada',
  LISTO: 'Listo para recoger',
  ENTREGADO: 'Vehículo entregado',
};

export default function ClienteOrdenesScreen() {
  const [ordenes, setOrdenes] = useState<any[]>([]);
  const [loading, setLoading] = useState(true);
  const [refreshing, setRefreshing] = useState(false);
  const [selected, setSelected] = useState<any | null>(null);
  const [historial, setHistorial] = useState<any[]>([]);
  const [loadingHistorial, setLoadingHistorial] = useState(false);

  useEffect(() => { loadOrdenes(); }, []);

  const loadOrdenes = async () => {
    try {
      const userStr = await AsyncStorage.getItem('user');
      const user = JSON.parse(userStr!);
      const response = await api.get(`/cliente/ordenes/${user.clienteId}`);
      setOrdenes(response.data);
    } catch (error) {
      console.error(error);
    } finally {
      setLoading(false);
      setRefreshing(false);
    }
  };

  const handleVerDetalle = async (item: any) => {
    setSelected(item);
    setLoadingHistorial(true);
    try {
      const response = await api.get(`/cliente/ordenes/${item.id}/historial`);
      setHistorial(response.data);
    } catch (error) {
      console.error(error);
    } finally {
      setLoadingHistorial(false);
    }
  };

  const getIconHistorial = (estadoNuevo: string) => {
    if (estadoNuevo === 'DIAGNOSTICO') return '🔍';
    return '🔄';
  };

  const renderOrden = ({ item }: { item: any }) => (
    <TouchableOpacity style={styles.card} onPress={() => handleVerDetalle(item)}>
      <View style={styles.cardHeader}>
        <Text style={styles.cardId}>#{item.id}</Text>
        <View style={[styles.badge, { backgroundColor: ESTADO_COLORS[item.estado] }]}>
          <Text style={styles.badgeText}>{item.estado.replace('_', ' ')}</Text>
        </View>
      </View>
      <Text style={styles.vehiculo}>
        {item.vehiculo?.marca} {item.vehiculo?.modelo} — {item.vehiculo?.matricula}
      </Text>
      <Text style={styles.descripcion}>{item.descripcionProblema}</Text>
      {item.diagnostico && (
        <Text style={styles.diagnostico}>🔍 {item.diagnostico}</Text>
      )}
      <View style={styles.cardFooter}>
        <Text style={styles.fecha}>📅 {formatFecha(item.fechaEntrada)}</Text>
        {item.precioFinal ? (
          <Text style={styles.precio}>💶 {item.precioFinal}€</Text>
        ) : item.precioEstimado ? (
          <Text style={styles.precioEst}>~{item.precioEstimado}€</Text>
        ) : null}
      </View>
      <Text style={styles.verDetalle}>Ver detalle →</Text>
    </TouchableOpacity>
  );

  if (loading) {
    return <View style={styles.center}><ActivityIndicator size="large" color="#E94560" /></View>;
  }

  return (
    <View style={styles.container}>
      <FlatList
        data={ordenes}
        keyExtractor={(item) => item.id.toString()}
        renderItem={renderOrden}
        contentContainerStyle={styles.list}
        refreshControl={<RefreshControl refreshing={refreshing} onRefresh={() => { setRefreshing(true); loadOrdenes(); }} tintColor="#E94560" />}
        ListEmptyComponent={<Text style={styles.empty}>No tienes órdenes de trabajo.</Text>}
      />

      <Modal visible={selected !== null} transparent animationType="slide">
        <View style={styles.modalOverlay}>
          <View style={styles.modalCard}>
            <ScrollView showsVerticalScrollIndicator={false}>
              <View style={styles.modalHeader}>
                <Text style={styles.modalTitle}>Orden #{selected?.id}</Text>
                <View style={[styles.badge, { backgroundColor: ESTADO_COLORS[selected?.estado ?? ''] }]}>
                  <Text style={styles.badgeText}>{selected?.estado?.replace('_', ' ')}</Text>
                </View>
              </View>

              <View style={styles.estadoBox}>
                <Text style={styles.estadoLabel}>Estado actual</Text>
                <Text style={[styles.estadoDesc, { color: ESTADO_COLORS[selected?.estado ?? ''] }]}>
                  {ESTADO_LABELS[selected?.estado ?? ''] ?? selected?.estado}
                </Text>
              </View>

              <Text style={styles.sectionTitle}>🚗 Vehículo</Text>
              <Text style={styles.detailValue}>
                {selected?.vehiculo?.marca} {selected?.vehiculo?.modelo}
              </Text>
              <Text style={styles.detailSub}>Matrícula: {selected?.vehiculo?.matricula}</Text>
              {selected?.vehiculo?.color && (
                <Text style={styles.detailSub}>Color: {selected?.vehiculo?.color}</Text>
              )}

              <Text style={styles.sectionTitle}>🔧 Descripción del Problema</Text>
              <Text style={styles.detailValue}>{selected?.descripcionProblema || '—'}</Text>

              {selected?.diagnostico ? (
                <>
                  <Text style={styles.sectionTitle}>🔍 Diagnóstico</Text>
                  <Text style={styles.detailValue}>{selected.diagnostico}</Text>
                </>
              ) : null}

              <Text style={styles.sectionTitle}>👨‍🔧 Mecánico asignado</Text>
              <Text style={styles.detailValue}>
                {selected?.mecanico?.usuario?.nombre} {selected?.mecanico?.usuario?.apellidos}
              </Text>
              {selected?.mecanico?.especialidad && (
                <Text style={styles.detailSub}>Especialidad: {selected.mecanico.especialidad}</Text>
              )}

              <Text style={styles.sectionTitle}>💶 Precios</Text>
              <Text style={styles.detailSub}>
                Estimado: {selected?.precioEstimado ? `${selected.precioEstimado}€` : '—'}
              </Text>
              <Text style={styles.detailSub}>
                Final: {selected?.precioFinal ? `${selected.precioFinal}€` : '—'}
              </Text>

              <Text style={styles.sectionTitle}>📅 Fechas</Text>
              <Text style={styles.detailSub}>
                Entrada: {formatFecha(selected?.fechaEntrada)}
              </Text>
              {selected?.fechaSalidaEstimada && (
                <Text style={styles.detailSub}>
                  Salida estimada: {formatFecha(selected.fechaSalidaEstimada)}
                </Text>
              )}

              <Text style={styles.sectionTitle}>📋 Historial de la Orden</Text>
              {loadingHistorial ? (
                <ActivityIndicator color="#E94560" style={{ marginTop: 8 }} />
              ) : historial.length === 0 ? (
                <Text style={styles.detailSub}>Sin historial disponible.</Text>
              ) : (
                historial.map((h, index) => (
                  <View key={index} style={styles.historialItem}>
                    <Text style={styles.historialIcon}>{getIconHistorial(h.estadoNuevo)}</Text>
                    <View style={styles.historialContent}>
                      <Text style={styles.historialTitulo}>
                        {h.estadoNuevo === 'DIAGNOSTICO'
                          ? 'Diagnóstico actualizado'
                          : `${h.estadoAnterior ?? 'INICIO'} → ${h.estadoNuevo}`}
                      </Text>
                      {h.observacion ? (
                        <Text style={styles.historialObs}>{h.observacion}</Text>
                      ) : null}
                      <Text style={styles.historialFecha}>
                        {formatFechaHora(h.fechaCambio)}
                      </Text>
                    </View>
                  </View>
                ))
              )}
            </ScrollView>

            <TouchableOpacity style={styles.btnCerrar} onPress={() => { setSelected(null); setHistorial([]); }}>
              <Text style={styles.btnCerrarText}>Cerrar</Text>
            </TouchableOpacity>
          </View>
        </View>
      </Modal>
    </View>
  );
}

const styles = StyleSheet.create({
  container: { flex: 1, backgroundColor: '#1A1A2E' },
  center: { flex: 1, backgroundColor: '#1A1A2E', justifyContent: 'center', alignItems: 'center' },
  list: { padding: 16, gap: 12 },
  card: { backgroundColor: '#1E1E3A', borderRadius: 12, padding: 16, borderLeftWidth: 4, borderLeftColor: '#E94560' },
  cardHeader: { flexDirection: 'row', justifyContent: 'space-between', alignItems: 'center', marginBottom: 8 },
  cardId: { color: '#A0A0B0', fontSize: 12, fontWeight: 'bold' },
  badge: { borderRadius: 12, paddingHorizontal: 10, paddingVertical: 4 },
  badgeText: { color: 'white', fontSize: 11, fontWeight: 'bold' },
  vehiculo: { color: '#EAEAEA', fontSize: 15, fontWeight: 'bold', marginBottom: 4 },
  descripcion: { color: '#A0A0B0', fontSize: 13, marginBottom: 4 },
  diagnostico: { color: '#A0A0B0', fontSize: 13, marginBottom: 8 },
  cardFooter: { flexDirection: 'row', justifyContent: 'space-between' },
  fecha: { color: '#A0A0B0', fontSize: 12 },
  precio: { color: '#2ECC71', fontSize: 12, fontWeight: 'bold' },
  precioEst: { color: '#F39C12', fontSize: 12 },
  verDetalle: { color: '#E94560', fontSize: 12, marginTop: 8, textAlign: 'right' },
  empty: { color: '#A0A0B0', textAlign: 'center', marginTop: 40 },
  modalOverlay: { flex: 1, backgroundColor: 'rgba(0,0,0,0.6)', justifyContent: 'flex-end' },
  modalCard: { backgroundColor: '#1E1E3A', borderTopLeftRadius: 20, borderTopRightRadius: 20, padding: 24, maxHeight: '90%' },
  modalHeader: { flexDirection: 'row', justifyContent: 'space-between', alignItems: 'center', marginBottom: 16 },
  modalTitle: { color: '#EAEAEA', fontSize: 18, fontWeight: 'bold' },
  estadoBox: { backgroundColor: '#252545', borderRadius: 10, padding: 14, marginBottom: 8 },
  estadoLabel: { color: '#A0A0B0', fontSize: 11, fontWeight: 'bold', marginBottom: 4 },
  estadoDesc: { fontSize: 15, fontWeight: 'bold' },
  sectionTitle: { color: '#E94560', fontSize: 13, fontWeight: 'bold', marginTop: 16, marginBottom: 4 },
  detailValue: { color: '#EAEAEA', fontSize: 14 },
  detailSub: { color: '#A0A0B0', fontSize: 13, marginTop: 2 },
  historialItem: { flexDirection: 'row', gap: 10, marginBottom: 10, paddingBottom: 10, borderBottomWidth: 1, borderBottomColor: '#2A2A4A' },
  historialIcon: { fontSize: 18, marginTop: 2 },
  historialContent: { flex: 1 },
  historialTitulo: { color: '#EAEAEA', fontSize: 13, fontWeight: 'bold' },
  historialObs: { color: '#A0A0B0', fontSize: 12, marginTop: 2 },
  historialFecha: { color: '#555575', fontSize: 11, marginTop: 4 },
  btnCerrar: { backgroundColor: '#252545', borderRadius: 8, padding: 14, alignItems: 'center', marginTop: 16 },
  btnCerrarText: { color: '#EAEAEA', fontWeight: 'bold' },
});