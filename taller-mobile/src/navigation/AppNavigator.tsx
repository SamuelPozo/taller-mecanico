import React, { useEffect, useState } from 'react';
import { NavigationContainer } from '@react-navigation/native';
import { createNativeStackNavigator } from '@react-navigation/native-stack';
import { ActivityIndicator, View } from 'react-native';
import { isAuthenticated, getUser } from '../store/authStore';
import LoginScreen from '../screens/auth/LoginScreen';
import MecanicoNavigator from './MecanicoNavigator';
import ClienteNavigator from './ClienteNavigator';
import RegisterScreen from '../screens/auth/RegisterScreen';

const Stack = createNativeStackNavigator();

export default function AppNavigator() {
  const [loading, setLoading] = useState(true);
  const [initialRoute, setInitialRoute] = useState('Login');

  useEffect(() => {
    checkAuth();
  }, []);

  const checkAuth = async () => {
    try {
      const authenticated = await isAuthenticated();
      if (authenticated) {
        const user = await getUser();
        if (user?.rol === 'MECANICO') {
          setInitialRoute('MecanicoTabs');
        } else if (user?.rol === 'CLIENTE') {
          setInitialRoute('ClienteTabs');
        }
      }
    } catch (error) {
      console.error(error);
    } finally {
      setLoading(false);
    }
  };

  if (loading) {
    return (
      <View style={{ flex: 1, justifyContent: 'center', alignItems: 'center', backgroundColor: '#1A1A2E' }}>
        <ActivityIndicator size="large" color="#E94560" />
      </View>
    );
  }

  return (
    <NavigationContainer>
      <Stack.Navigator initialRouteName={initialRoute} screenOptions={{ headerShown: false }}>
        <Stack.Screen name="Login" component={LoginScreen} />
        <Stack.Screen name="Register" component={RegisterScreen} />
        <Stack.Screen name="MecanicoTabs" component={MecanicoNavigator} />
        <Stack.Screen name="ClienteTabs" component={ClienteNavigator} />
      </Stack.Navigator>
    </NavigationContainer>
  );
}