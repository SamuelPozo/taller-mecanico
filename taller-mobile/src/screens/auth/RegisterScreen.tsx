import React, { useState } from 'react';
import {
  View, Text, TextInput, TouchableOpacity,
  StyleSheet, ActivityIndicator, Alert,
  KeyboardAvoidingView, Platform, ScrollView
} from 'react-native';
import api from '../../api/api';
import { validarEmail, validarTelefono } from '../../utils/validaciones';

export default function RegisterScreen({ navigation }: any) {
  const [nombre, setNombre] = useState('');
  const [apellidos, setApellidos] = useState('');
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [telefono, setTelefono] = useState('');
  const [loading, setLoading] = useState(false);

  const handleRegister = async () => {
    if (!nombre.trim() || !apellidos.trim() || !email.trim() || !password.trim()) {
      Alert.alert('Error', 'Los campos nombre, apellidos, email y contraseña son obligatorios.');
      return;
    }
    if (!validarEmail(email)) {
      Alert.alert('Error', 'El correo electrónico no es válido.');
      return;
    }
    if (password.length < 6) {
      Alert.alert('Error', 'La contraseña debe tener al menos 6 caracteres.');
      return;
    }
    if (telefono.trim() && !validarTelefono(telefono)) {
      Alert.alert('Error', 'El teléfono no es válido. Debe tener 9 dígitos y empezar por 6, 7, 8 o 9.');
      return;
    }

    setLoading(true);
    try {
      await api.post('/auth/register', {
        nombre: nombre.trim(),
        apellidos: apellidos.trim(),
        email: email.trim().toLowerCase(),
        password,
        telefono: telefono.trim(),
        rol: 'CLIENTE'
      });
      Alert.alert('¡Cuenta creada!', 'Ya puedes iniciar sesión con tus credenciales.', [
        { text: 'Ir al login', onPress: () => navigation.replace('Login') }
      ]);
    } catch (error: any) {
      const msg = error.message || 'Error inesperado.';
      Alert.alert('Error', msg);
    } finally {
      setLoading(false);
    }
  };

  return (
    <KeyboardAvoidingView
      style={styles.container}
      behavior={Platform.OS === 'ios' ? 'padding' : 'height'}
    >
      <ScrollView contentContainerStyle={styles.scrollContent}>
        <View style={styles.card}>
          <Text style={styles.icon}>🔧</Text>
          <Text style={styles.title}>Crear Cuenta</Text>
          <Text style={styles.subtitle}>Regístrate para hacer seguimiento de tu vehículo</Text>

          <Text style={styles.label}>NOMBRE *</Text>
          <TextInput style={styles.input} placeholder="Tu nombre" placeholderTextColor="#555575"
            value={nombre} onChangeText={setNombre} />

          <Text style={styles.label}>APELLIDOS *</Text>
          <TextInput style={styles.input} placeholder="Tus apellidos" placeholderTextColor="#555575"
            value={apellidos} onChangeText={setApellidos} />

          <Text style={styles.label}>CORREO ELECTRÓNICO *</Text>
          <TextInput style={styles.input} placeholder="tu@email.com" placeholderTextColor="#555575"
            value={email} onChangeText={setEmail} keyboardType="email-address" autoCapitalize="none" />

          <Text style={styles.label}>CONTRASEÑA * (mínimo 6 caracteres)</Text>
          <TextInput style={styles.input} placeholder="Mínimo 6 caracteres" placeholderTextColor="#555575"
            value={password} onChangeText={setPassword} secureTextEntry />

          <Text style={styles.label}>TELÉFONO</Text>
          <TextInput style={styles.input} placeholder="600000000" placeholderTextColor="#555575"
            value={telefono} onChangeText={setTelefono} keyboardType="phone-pad" />

          <TouchableOpacity
            style={[styles.button, loading && styles.buttonDisabled]}
            onPress={handleRegister}
            disabled={loading}
          >
            {loading ? (
              <ActivityIndicator color="white" />
            ) : (
              <Text style={styles.buttonText}>CREAR CUENTA</Text>
            )}
          </TouchableOpacity>

          <TouchableOpacity onPress={() => navigation.replace('Login')} style={styles.linkContainer}>
            <Text style={styles.link}>¿Ya tienes cuenta? Inicia sesión</Text>
          </TouchableOpacity>
        </View>
      </ScrollView>
    </KeyboardAvoidingView>
  );
}

const styles = StyleSheet.create({
  container: { flex: 1, backgroundColor: '#1A1A2E' },
  scrollContent: { flexGrow: 1, justifyContent: 'center', alignItems: 'center', padding: 24 },
  card: { backgroundColor: '#1E1E3A', borderRadius: 16, padding: 32, width: '100%', maxWidth: 400 },
  icon: { fontSize: 48, textAlign: 'center', marginBottom: 12 },
  title: { fontSize: 26, fontWeight: 'bold', color: '#EAEAEA', textAlign: 'center', marginBottom: 6 },
  subtitle: { fontSize: 13, color: '#A0A0B0', textAlign: 'center', marginBottom: 20 },
  label: { fontSize: 11, fontWeight: 'bold', color: '#A0A0B0', marginBottom: 6, marginTop: 12 },
  input: {
    backgroundColor: '#252545', borderRadius: 8, borderWidth: 1.5,
    borderColor: '#2A2A4A', color: '#EAEAEA', padding: 12, fontSize: 13
  },
  button: { backgroundColor: '#E94560', borderRadius: 8, padding: 14, alignItems: 'center', marginTop: 24 },
  buttonDisabled: { backgroundColor: '#a82d45' },
  buttonText: { color: 'white', fontWeight: 'bold', fontSize: 14 },
  linkContainer: { alignItems: 'center', marginTop: 16 },
  link: { color: '#A0A0B0', fontSize: 13 }
});