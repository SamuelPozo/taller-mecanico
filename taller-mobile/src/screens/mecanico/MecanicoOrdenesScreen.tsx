import React, { useEffect, useState } from 'react';
import {
  View, Text, StyleSheet, FlatList, ActivityIndicator,
  TouchableOpacity, RefreshControl, Modal, ScrollView, TextInput, Alert
} from 'react-native';
import api from '../../api/api';
import { OrdenTrabajo } from '../../types';
import AsyncStorage from '@react-native-async-storage/async-storage';
import { formatFecha, formatFechaHora } from '../../utils/fecha';

const ESTADO_COLORS: Record<string, string> = {
  PENDIENTE: '#F39C12',
  EN_PROCESO: '#3498DB',
  DIAGNOSTICADO: '#9B59B6',
  REPARADO: '#1ABC9C',
  LISTO: '#2ECC71',
  ENTREGADO: '#555575',
};

const ESTADOS = ['PENDIENTE', 'EN_PROCESO', 'DIAGNOSTICADO', 'REPARADO', 'LISTO', 'ENTREGADO'];

export default function MecanicoOrdenesScreen() {
  const [ordenes, setOrdenes] = useState<OrdenTrabajo[]>([]);
  const [loading, setLoading] = useState(true);
  const [refreshing, setRefreshing] = useState(false);
  const [selected, setSelected] = useState<OrdenTrabajo | null>(null);
  const [historial, setHistorial] = useState<any[]>([]);
  const [loadingHistorial, setLoadingHistorial] = useState(false);
  const [modalAcciones, setModalAcciones] = useState(false);
  const [nuevoEstado, setNuevoEstado] = useState('');
  const [nuevoDiagnostico, setNuevoDiagnostico] = useState('');
  const [observacion, setObservacion] = useState('');
  const [saving, setSaving] = useState(false);

  useEffect(() => { loadOrdenes(); }, []);

  const loadOrdenes = async () => {
    try {
      const userStr = await AsyncStorage.getItem('user');
      const user = JSON.parse(userStr!);
      const response = await api.get(`/mecanico/ordenes/${user.mecanicoId}`);
      setOrdenes(response.data);
    } catch (error) {
      console.error(error);
    } finally {
      setLoading(false);
      setRefreshing(false);
    }
  };

  const handleVerDetalle = async (item: OrdenTrabajo) => {
    setSelected(item);
    setNuevoEstado(item.estado);
    setNuevoDiagnostico((item as any).diagnostico ?? '');
    setLoadingHistorial(true);
    try {
      const response = await api.get(`/mecanico/ordenes/${item.id}/historial`);
      setHistorial(response.data);
    } catch (error) {
      console.error(error);
    } finally {
      setLoadingHistorial(false);
    }
  };

  const handleGuardarCambios = async () => {
    if (!selected) return;
    setSaving(true);
    try {
      if (nuevoEstado !== selected.estado) {
        await api.patch(`/mecanico/ordenes/${selected.id}/estado`, {
          estado: nuevoEstado,
          observacion: observacion,
        });
      }
      if (nuevoDiagnostico !== (selected as any).diagnostico) {
        await api.patch(`/mecanico/ordenes/${selected.id}/diagnostico`, {
          diagnostico: nuevoDiagnostico,
        });
      }
      Alert.alert('Éxito', 'Orden actualizada correctamente.');
      setModalAcciones(false);
      setObservacion('');
      loadOrdenes();
      setSelected(null);
      setHistorial([]);
    } catch (error) {
      Alert.alert('Error', 'No se pudo actualizar la orden.');
    } finally {
      setSaving(false);
    }
  };

  const getIconHistorial = (estadoNuevo: string) => {
    if (estadoNuevo === 'DIAGNOSTICO') return '🔍';
    return '🔄';
  };

  const renderOrden = ({ item }: { item: OrdenTrabajo }) => (
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
      <View style={styles.cardFooter}>
        <Text style={styles.fecha}>📅 {formatFecha(item.fechaEntrada)}</Text>
        {item.precioEstimado && (
          <Text style={styles.precio}>💶 {item.precioEstimado}€</Text>
        )}
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
        ListEmptyComponent={<Text style={styles.empty}>No hay órdenes asignadas.</Text>}
      />

      {/* MODAL DETALLE */}
      <Modal visible={selected !== null && !modalAcciones} transparent animationType="slide">
        <View style={styles.modalOverlay}>
          <View style={styles.modalCard}>
            <ScrollView showsVerticalScrollIndicator={false}>
              <View style={styles.modalHeader}>
                <Text style={styles.modalTitle}>Orden #{selected?.id}</Text>
                <View style={[styles.badge, { backgroundColor: ESTADO_COLORS[selected?.estado ?? ''] }]}>
                  <Text style={styles.badgeText}>{selected?.estado?.replace('_', ' ')}</Text>
                </View>
              </View>

              <Text style={styles.sectionTitle}>🚗 Vehículo</Text>
              <Text style={styles.detailValue}>{selected?.vehiculo?.marca} {selected?.vehiculo?.modelo}</Text>
              <Text style={styles.detailSub}>Matrícula: {selected?.vehiculo?.matricula}</Text>
              {selected?.vehiculo?.color && <Text style={styles.detailSub}>Color: {selected?.vehiculo?.color}</Text>}

              <Text style={styles.sectionTitle}>👤 Cliente</Text>
              <Text style={styles.detailValue}>
                {(selected?.vehiculo as any)?.cliente?.usuario?.nombre}{' '}
                {(selected?.vehiculo as any)?.cliente?.usuario?.apellidos}
              </Text>

              <Text style={styles.sectionTitle}>🔧 Descripción del Problema</Text>
              <Text style={styles.detailValue}>{selected?.descripcionProblema || '—'}</Text>

              {(selected as any)?.diagnostico ? (
                <>
                  <Text style={styles.sectionTitle}>🔍 Diagnóstico</Text>
                  <Text style={styles.detailValue}>{(selected as any).diagnostico}</Text>
                </>
              ) : null}

              <Text style={styles.sectionTitle}>💶 Precios</Text>
              <Text style={styles.detailSub}>Estimado: {selected?.precioEstimado ? `${selected.precioEstimado}€` : '—'}</Text>
              <Text style={styles.detailSub}>Final: {selected?.precioFinal ? `${selected.precioFinal}€` : '—'}</Text>

              <Text style={styles.sectionTitle}>📅 Fechas</Text>
              <Text style={styles.detailSub}>Entrada: {formatFecha(selected?.fechaEntrada)}</Text>

              <Text style={styles.sectionTitle}>📋 Historial</Text>
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
                      {h.observacion ? <Text style={styles.historialObs}>{h.observacion}</Text> : null}
                      <Text style={styles.historialFecha}>{formatFechaHora(h.fechaCambio)}</Text>
                    </View>
                  </View>
                ))
              )}
            </ScrollView>

            <TouchableOpacity style={[styles.btnAcciones, { marginTop: 16 }]} onPress={() => setModalAcciones(true)}>
              <Text style={{ color: 'white', fontWeight: 'bold', fontSize: 14 }}>✏️ Actualizar orden</Text>
            </TouchableOpacity>
            <TouchableOpacity style={styles.btnCerrar} onPress={() => { setSelected(null); setHistorial([]); }}>
              <Text style={{ color: '#EAEAEA', fontWeight: 'bold', fontSize: 14 }}>Cerrar</Text>
            </TouchableOpacity>
          </View>
        </View>
      </Modal>

      {/* MODAL ACCIONES */}
      <Modal visible={modalAcciones} transparent animationType="slide">
        <View style={styles.modalOverlay}>
          <View style={styles.modalCard}>
            <ScrollView showsVerticalScrollIndicator={false}>
              <Text style={styles.modalTitle}>Actualizar Orden #{selected?.id}</Text>

              <Text style={styles.sectionTitle}>Estado</Text>
              <View style={styles.estadosGrid}>
                {ESTADOS.map(e => (
                  <TouchableOpacity
                    key={e}
                    style={[styles.estadoBtn, nuevoEstado === e && { backgroundColor: ESTADO_COLORS[e] }]}
                    onPress={() => setNuevoEstado(e)}
                  >
                    <Text style={[styles.estadoBtnText, nuevoEstado === e && styles.estadoBtnTextActive]}>
                      {e.replace('_', ' ')}
                    </Text>
                  </TouchableOpacity>
                ))}
              </View>

              <Text style={styles.sectionTitle}>Diagnóstico</Text>
              <TextInput
                style={[styles.input, { height: 80, textAlignVertical: 'top' }]}
                value={nuevoDiagnostico}
                onChangeText={setNuevoDiagnostico}
                placeholder="Escribe el diagnóstico..."
                placeholderTextColor="#555575"
                multiline
              />

              <Text style={styles.sectionTitle}>Observación (opcional)</Text>
              <TextInput
                style={styles.input}
                value={observacion}
                onChangeText={setObservacion}
                placeholder="Observación sobre el cambio..."
                placeholderTextColor="#555575"
              />
            </ScrollView>

            <View style={styles.modalButtons}>
              <TouchableOpacity style={[styles.btnCerrar, { flex: 1, marginTop: 0 }]} onPress={() => setModalAcciones(false)}>
                <Text style={{ color: '#EAEAEA', fontWeight: 'bold', fontSize: 14 }}>Cancelar</Text>
              </TouchableOpacity>
              <TouchableOpacity style={[styles.btnAcciones, { flex: 1, marginTop: 0 }]} onPress={handleGuardarCambios} disabled={saving}>
                {saving ? <ActivityIndicator color="white" /> : <Text style={{ color: 'white', fontWeight: 'bold', fontSize: 14 }}>Guardar</Text>}
              </TouchableOpacity>
            </View>
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
  descripcion: { color: '#A0A0B0', fontSize: 13, marginBottom: 8 },
  cardFooter: { flexDirection: 'row', justifyContent: 'space-between' },
  fecha: { color: '#A0A0B0', fontSize: 12 },
  precio: { color: '#2ECC71', fontSize: 12, fontWeight: 'bold' },
  verDetalle: { color: '#E94560', fontSize: 12, marginTop: 8, textAlign: 'right' },
  empty: { color: '#A0A0B0', textAlign: 'center', marginTop: 40 },
  modalOverlay: { flex: 1, backgroundColor: 'rgba(0,0,0,0.6)', justifyContent: 'flex-end' },
  modalCard: { backgroundColor: '#1E1E3A', borderTopLeftRadius: 20, borderTopRightRadius: 20, padding: 24, maxHeight: '90%' },
  modalHeader: { flexDirection: 'row', justifyContent: 'space-between', alignItems: 'center', marginBottom: 16 },
  modalTitle: { color: '#EAEAEA', fontSize: 18, fontWeight: 'bold', marginBottom: 8 },
  sectionTitle: { color: '#E94560', fontSize: 13, fontWeight: 'bold', marginTop: 16, marginBottom: 4 },
  detailValue: { color: '#EAEAEA', fontSize: 14 },
  detailSub: { color: '#A0A0B0', fontSize: 13, marginTop: 2 },
  historialItem: { flexDirection: 'row', gap: 10, marginBottom: 10, paddingBottom: 10, borderBottomWidth: 1, borderBottomColor: '#2A2A4A' },
  historialIcon: { fontSize: 18, marginTop: 2 },
  historialContent: { flex: 1 },
  historialTitulo: { color: '#EAEAEA', fontSize: 13, fontWeight: 'bold' },
  historialObs: { color: '#A0A0B0', fontSize: 12, marginTop: 2 },
  historialFecha: { color: '#555575', fontSize: 11, marginTop: 4 },
  estadosGrid: { flexDirection: 'row', flexWrap: 'wrap', gap: 8, marginTop: 4 },
  estadoBtn: { borderRadius: 20, paddingHorizontal: 12, paddingVertical: 6, backgroundColor: '#252545', borderWidth: 1, borderColor: '#2A2A4A' },
  estadoBtnText: { color: '#A0A0B0', fontSize: 11 },
  estadoBtnTextActive: { color: 'white', fontWeight: 'bold' },
  input: { backgroundColor: '#252545', borderRadius: 8, borderWidth: 1.5, borderColor: '#2A2A4A', color: '#EAEAEA', padding: 12, fontSize: 13 },
  btnCerrar: { backgroundColor: '#252545', borderRadius: 8, padding: 14, alignItems: 'center', marginTop: 8 },
  btnCerrarText: { color: '#EAEAEA', fontWeight: 'bold', fontSize: 14 },
  btnAcciones: { backgroundColor: '#E94560', borderRadius: 8, padding: 14, alignItems: 'center', marginTop: 8 },
  btnAccionesText: { color: 'white', fontWeight: 'bold', fontSize: 14 },
  modalButtons: { flexDirection: 'row', gap: 12, marginTop: 16 },
});