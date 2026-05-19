import React, { useEffect, useState } from 'react';
import {
  View, Text, StyleSheet, FlatList, ActivityIndicator,
  RefreshControl, TouchableOpacity, Modal, TextInput, Alert
} from 'react-native';
import AsyncStorage from '@react-native-async-storage/async-storage';
import api from '../../api/api';
import { formatFecha } from '../../utils/fecha';

const ESTADO_COLORS: Record<string, string> = {
  PENDIENTE: '#F39C12',
  APROBADA: '#2ECC71',
  RECHAZADA: '#E94560',
};

const TIPOS = ['VACACIONES', 'DIA_LIBRE', 'BAJA', 'OTRO'];

export default function MecanicoAusenciasScreen() {
  const [ausencias, setAusencias] = useState<any[]>([]);
  const [loading, setLoading] = useState(true);
  const [refreshing, setRefreshing] = useState(false);
  const [modalVisible, setModalVisible] = useState(false);
  const [tipo, setTipo] = useState('VACACIONES');
  const [fechaInicio, setFechaInicio] = useState('');
  const [fechaFin, setFechaFin] = useState('');
  const [motivo, setMotivo] = useState('');
  const [saving, setSaving] = useState(false);

  useEffect(() => { loadAusencias(); }, []);

  const loadAusencias = async () => {
    try {
      const userStr = await AsyncStorage.getItem('user');
      const user = JSON.parse(userStr!);
      const response = await api.get(`/mecanico/ausencias/${user.mecanicoId}`);
      setAusencias(response.data);
    } catch (error) {
      console.error(error);
    } finally {
      setLoading(false);
      setRefreshing(false);
    }
  };

  const handleSolicitar = async () => {
    if (!fechaInicio || !fechaFin) {
      Alert.alert('Error', 'Las fechas de inicio y fin son obligatorias.');
      return;
    }
    const dateRegex = /^\d{2}\/\d{2}\/\d{4}$/;
    if (!dateRegex.test(fechaInicio) || !dateRegex.test(fechaFin)) {
      Alert.alert('Error', 'El formato de fecha debe ser DD/MM/AAAA.');
      return;
    }
    if (fechaInicio > fechaFin) {
      Alert.alert('Error', 'La fecha de inicio no puede ser posterior a la fecha de fin.');
      return;
    }

    setSaving(true);
    try {
      const userStr = await AsyncStorage.getItem('user');
      const user = JSON.parse(userStr!);
      const convertirFecha = (fecha: string) => {
      const [dia, mes, anio] = fecha.split('/');
      return `${anio}-${mes}-${dia}`;
    };

    await api.post('/mecanico/ausencias', {
      mecanicoId: user.mecanicoId,
      tipo,
      fechaInicio: convertirFecha(fechaInicio),
      fechaFin: convertirFecha(fechaFin),
      motivo,
    });

      Alert.alert('Solicitud enviada', 'Tu solicitud de ausencia ha sido enviada para aprobación.');
      setModalVisible(false);
      setTipo('VACACIONES');
      setFechaInicio('');
      setFechaFin('');
      setMotivo('');
      loadAusencias();
    } catch (error: any) {
      const msg = error.message || 'Error inesperado.';
      Alert.alert('Error', msg);
    } finally {
      setSaving(false);
    }
  };

  const getEstado = (aprobada: boolean | null) => {
    if (aprobada === null || aprobada === undefined) return 'PENDIENTE';
    return aprobada ? 'APROBADA' : 'RECHAZADA';
  };

  const renderAusencia = ({ item }: { item: any }) => {
    const estado = getEstado(item.aprobada);
    return (
      <View style={styles.card}>
        <View style={styles.cardHeader}>
          <Text style={styles.tipo}>{item.tipo}</Text>
          <View style={[styles.badge, { backgroundColor: ESTADO_COLORS[estado] }]}>
            <Text style={styles.badgeText}>{estado}</Text>
          </View>
        </View>
        <Text style={styles.fechas}>📅 {formatFecha(item.fechaInicio)} → {formatFecha(item.fechaFin)}</Text>
        {item.motivo ? <Text style={styles.motivo}>💬 {item.motivo}</Text> : null}
      </View>
    );
  };

  if (loading) {
    return <View style={styles.center}><ActivityIndicator size="large" color="#E94560" /></View>;
  }

  return (
    <View style={styles.container}>
      <FlatList
        data={ausencias}
        keyExtractor={(item) => item.id.toString()}
        renderItem={renderAusencia}
        contentContainerStyle={styles.list}
        refreshControl={<RefreshControl refreshing={refreshing} onRefresh={() => { setRefreshing(true); loadAusencias(); }} tintColor="#E94560" />}
        ListEmptyComponent={<Text style={styles.empty}>No hay ausencias registradas.</Text>}
        ListFooterComponent={<View style={{ height: 80 }} />}
      />

      <TouchableOpacity style={styles.fab} onPress={() => setModalVisible(true)}>
        <Text style={styles.fabText}>+ Solicitar</Text>
      </TouchableOpacity>

      <Modal visible={modalVisible} transparent animationType="slide">
        <View style={styles.modalOverlay}>
          <View style={styles.modalCard}>
            <Text style={styles.modalTitle}>Solicitar Ausencia</Text>

            <Text style={styles.label}>TIPO</Text>
            <View style={styles.tiposRow}>
              {TIPOS.map(t => (
                <TouchableOpacity
                  key={t}
                  style={[styles.tipoBtn, tipo === t && styles.tipoBtnActive]}
                  onPress={() => setTipo(t)}
                >
                  <Text style={[styles.tipoBtnText, tipo === t && styles.tipoBtnTextActive]}>
                    {t}
                  </Text>
                </TouchableOpacity>
              ))}
            </View>

            <Text style={styles.label}>FECHA INICIO *</Text>
            <TextInput
              style={styles.input}
              placeholder="DD/MM/AAAA"
              placeholderTextColor="#555575"
              value={fechaInicio}
              onChangeText={setFechaInicio}
            />

            <Text style={styles.label}>FECHA FIN *</Text>
            <TextInput
              style={styles.input}
              placeholder="DD/MM/AAAA"
              placeholderTextColor="#555575"
              value={fechaFin}
              onChangeText={setFechaFin}
            />

            <Text style={styles.label}>MOTIVO</Text>
            <TextInput
              style={[styles.input, { height: 80, textAlignVertical: 'top' }]}
              placeholder="Describe el motivo (opcional)"
              placeholderTextColor="#555575"
              value={motivo}
              onChangeText={setMotivo}
              multiline
            />

            <View style={styles.modalButtons}>
              <TouchableOpacity style={styles.btnCancelar} onPress={() => setModalVisible(false)}>
                <Text style={styles.btnCancelarText}>Cancelar</Text>
              </TouchableOpacity>
              <TouchableOpacity style={styles.btnEnviar} onPress={handleSolicitar} disabled={saving}>
                {saving ? <ActivityIndicator color="white" /> : <Text style={styles.btnEnviarText}>Enviar</Text>}
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
  tipo: { color: '#EAEAEA', fontWeight: 'bold', fontSize: 15 },
  badge: { borderRadius: 12, paddingHorizontal: 10, paddingVertical: 4 },
  badgeText: { color: 'white', fontSize: 11, fontWeight: 'bold' },
  fechas: { color: '#A0A0B0', fontSize: 13, marginBottom: 4 },
  motivo: { color: '#A0A0B0', fontSize: 13 },
  empty: { color: '#A0A0B0', textAlign: 'center', marginTop: 40 },
  fab: {
    position: 'absolute', bottom: 24, right: 24,
    backgroundColor: '#E94560', borderRadius: 30,
    paddingHorizontal: 20, paddingVertical: 14,
    elevation: 4,
  },
  fabText: { color: 'white', fontWeight: 'bold', fontSize: 14 },
  modalOverlay: { flex: 1, backgroundColor: 'rgba(0,0,0,0.6)', justifyContent: 'flex-end' },
  modalCard: { backgroundColor: '#1E1E3A', borderTopLeftRadius: 20, borderTopRightRadius: 20, padding: 24 },
  modalTitle: { color: '#EAEAEA', fontSize: 18, fontWeight: 'bold', marginBottom: 16 },
  label: { color: '#A0A0B0', fontSize: 11, fontWeight: 'bold', marginBottom: 6, marginTop: 12 },
  input: { backgroundColor: '#252545', borderRadius: 8, borderWidth: 1.5, borderColor: '#2A2A4A', color: '#EAEAEA', padding: 12, fontSize: 13 },
  tiposRow: { flexDirection: 'row', flexWrap: 'wrap', gap: 8 },
  tipoBtn: { borderRadius: 20, paddingHorizontal: 12, paddingVertical: 6, backgroundColor: '#252545', borderWidth: 1, borderColor: '#2A2A4A' },
  tipoBtnActive: { backgroundColor: '#E94560', borderColor: '#E94560' },
  tipoBtnText: { color: '#A0A0B0', fontSize: 12 },
  tipoBtnTextActive: { color: 'white', fontWeight: 'bold' },
  modalButtons: { flexDirection: 'row', gap: 12, marginTop: 20 },
  btnCancelar: { flex: 1, borderRadius: 8, padding: 14, alignItems: 'center', backgroundColor: '#252545' },
  btnCancelarText: { color: '#A0A0B0', fontWeight: 'bold' },
  btnEnviar: { flex: 1, borderRadius: 8, padding: 14, alignItems: 'center', backgroundColor: '#E94560' },
  btnEnviarText: { color: 'white', fontWeight: 'bold' },
});