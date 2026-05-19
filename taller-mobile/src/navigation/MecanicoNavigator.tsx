import React, { useEffect, useState } from 'react';
import { Text, View } from 'react-native';
import { createBottomTabNavigator } from '@react-navigation/bottom-tabs';
import MecanicoOrdenesScreen from '../screens/mecanico/MecanicoOrdenesScreen';
import MecanicoHorariosScreen from '../screens/mecanico/MecanicoHorariosScreen';
import MecanicoAusenciasScreen from '../screens/mecanico/MecanicoAusenciasScreen';
import MecanicoPerfilScreen from '../screens/mecanico/MecanicoPerfilScreen';
import NotificacionesScreen from '../screens/shared/NotificacionesScreen';
import api from '../api/api';

const Tab = createBottomTabNavigator();

export default function MecanicoNavigator() {
  const [noLeidas, setNoLeidas] = useState(0);

  useEffect(() => {
    cargarContador();
    const interval = setInterval(cargarContador, 30000);
    return () => clearInterval(interval);
  }, []);

  const cargarContador = async () => {
    try {
      const response = await api.get('/notificaciones/no-leidas');
      setNoLeidas(response.data.count);
    } catch (error) {
      console.error(error);
    }
  };

  return (
    <Tab.Navigator
      screenOptions={{
        headerStyle: { backgroundColor: '#1E1E3A' },
        headerTintColor: '#EAEAEA',
        tabBarStyle: { backgroundColor: '#1E1E3A', borderTopColor: '#2A2A4A' },
        tabBarActiveTintColor: '#E94560',
        tabBarInactiveTintColor: '#A0A0B0',
      }}
    >
      <Tab.Screen name="Órdenes" component={MecanicoOrdenesScreen}
        options={{ tabBarIcon: () => <Text>📋</Text> }} />
      <Tab.Screen name="Horarios" component={MecanicoHorariosScreen}
        options={{ tabBarIcon: () => <Text>📅</Text> }} />
      <Tab.Screen name="Ausencias" component={MecanicoAusenciasScreen}
        options={{ tabBarIcon: () => <Text>🏖️</Text> }} />
      <Tab.Screen name="Notificaciones" component={NotificacionesScreen}
        options={{
          tabBarIcon: () => (
            <View>
              <Text>🔔</Text>
              {noLeidas > 0 && (
                <View style={{
                  position: 'absolute', top: -4, right: -8,
                  backgroundColor: '#E94560', borderRadius: 8,
                  minWidth: 16, height: 16, justifyContent: 'center', alignItems: 'center'
                }}>
                  <Text style={{ color: 'white', fontSize: 9, fontWeight: 'bold' }}>
                    {noLeidas > 9 ? '9+' : noLeidas}
                  </Text>
                </View>
              )}
            </View>
          ),
          tabBarLabel: 'Notificaciones'
        }}
      />
      <Tab.Screen name="Perfil" component={MecanicoPerfilScreen}
        options={{ tabBarIcon: () => <Text>👤</Text> }} />
    </Tab.Navigator>
  );
}