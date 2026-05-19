import React, { useState } from 'react';
import {
  View,
  Text,
  TextInput,
  TouchableOpacity,
  StyleSheet,
  ActivityIndicator,
  Alert,
  KeyboardAvoidingView,
  Platform,
  ScrollView,
} from 'react-native';
import { saveAuth } from '../../store/authStore';
import api from '../../api/api';
import { AuthResponse } from '../../types';

export default function LoginScreen({ navigation }: any) {
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [loading, setLoading] = useState(false);
  const [showPassword, setShowPassword] = useState(false);

  const handleLogin = async () => {
    if (!email || !password) {
      Alert.alert('Error', 'Por favor introduce el email y la contraseña');
      return;
    }

    setLoading(true);
    try {
      const response = await api.post<AuthResponse>('/auth/login', { email, password });
      const data = response.data;

      if (data.rol === 'MECANICO') {
        await saveAuth(data);
        navigation.replace('MecanicoTabs');
      } else if (data.rol === 'CLIENTE') {
        await saveAuth(data);
        navigation.replace('ClienteTabs');
      } else {
        Alert.alert('Acceso denegado', 'Esta app es solo para mecánicos y clientes.');
      }
    } catch (error: any) {
      Alert.alert('Error', error.message || 'Credenciales incorrectas. Inténtalo de nuevo.');
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
          <Text style={styles.title}>Taller Mecánico</Text>
          <Text style={styles.subtitle}>Acceso para mecánicos y clientes</Text>

          <Text style={styles.label}>CORREO ELECTRÓNICO</Text>
          <TextInput
            style={styles.input}
            placeholder="usuario@taller.com"
            placeholderTextColor="#555575"
            value={email}
            onChangeText={setEmail}
            keyboardType="email-address"
            autoCapitalize="none"
          />

          <Text style={styles.label}>CONTRASEÑA</Text>
          <View style={styles.passwordContainer}>
            <TextInput
              style={styles.passwordInput}
              placeholder="••••••••"
              placeholderTextColor="#555575"
              value={password}
              onChangeText={setPassword}
              secureTextEntry={!showPassword}
            />
            <TouchableOpacity
              style={styles.eyeButton}
              onPress={() => setShowPassword(!showPassword)}
            >
              <Text style={styles.eyeIcon}>{showPassword ? '🙈' : '👁️'}</Text>
            </TouchableOpacity>
          </View>

          <TouchableOpacity
            style={[styles.button, loading && styles.buttonDisabled]}
            onPress={handleLogin}
            disabled={loading}
          >
            {loading ? (
              <ActivityIndicator color="white" />
            ) : (
              <Text style={styles.buttonText}>INICIAR SESIÓN</Text>
            )}
          </TouchableOpacity>

          <TouchableOpacity onPress={() => navigation.replace('Register')} style={{ alignItems: 'center', marginTop: 16 }}>
            <Text style={{ color: '#A0A0B0', fontSize: 13 }}>¿No tienes cuenta? Regístrate</Text>
          </TouchableOpacity>
        </View>
      </ScrollView>
    </KeyboardAvoidingView>
  );
}

const styles = StyleSheet.create({
  container: {
    flex: 1,
    backgroundColor: '#1A1A2E',
  },
  scrollContent: {
    flexGrow: 1,
    justifyContent: 'center',
    alignItems: 'center',
    padding: 24,
  },
  card: {
    backgroundColor: '#1E1E3A',
    borderRadius: 16,
    padding: 32,
    width: '100%',
    maxWidth: 400,
  },
  icon: {
    fontSize: 48,
    textAlign: 'center',
    marginBottom: 12,
  },
  title: {
    fontSize: 26,
    fontWeight: 'bold',
    color: '#EAEAEA',
    textAlign: 'center',
    marginBottom: 6,
  },
  subtitle: {
    fontSize: 13,
    color: '#A0A0B0',
    textAlign: 'center',
    marginBottom: 28,
  },
  label: {
    fontSize: 11,
    fontWeight: 'bold',
    color: '#A0A0B0',
    marginBottom: 6,
    marginTop: 12,
  },
  input: {
    backgroundColor: '#252545',
    borderRadius: 8,
    borderWidth: 1.5,
    borderColor: '#2A2A4A',
    color: '#EAEAEA',
    padding: 12,
    fontSize: 13,
  },
  passwordContainer: {
    flexDirection: 'row',
    alignItems: 'center',
    backgroundColor: '#252545',
    borderRadius: 8,
    borderWidth: 1.5,
    borderColor: '#2A2A4A',
  },
  passwordInput: {
    flex: 1,
    color: '#EAEAEA',
    padding: 12,
    fontSize: 13,
  },
  eyeButton: {
    padding: 12,
  },
  eyeIcon: {
    fontSize: 18,
  },
  button: {
    backgroundColor: '#E94560',
    borderRadius: 8,
    padding: 14,
    alignItems: 'center',
    marginTop: 24,
  },
  buttonDisabled: {
    backgroundColor: '#a82d45',
  },
  buttonText: {
    color: 'white',
    fontWeight: 'bold',
    fontSize: 14,
  },
});