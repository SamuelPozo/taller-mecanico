export interface Usuario {
  id: number;
  nombre: string;
  apellidos: string;
  email: string;
  telefono?: string;
  rol: 'ADMIN' | 'MECANICO' | 'CLIENTE';
}

export interface Mecanico {
  id: number;
  usuario: Usuario;
  especialidad?: string;
  turno?: 'MANANA' | 'TARDE' | 'NOCHE';
  numEmpleado: string;
  anosExperiencia?: number;
}

export interface Cliente {
  id: number;
  usuario: Usuario;
  nif?: string;
  direccion?: string;
}

export interface Vehiculo {
  id: number;
  matricula: string;
  marca: string;
  modelo: string;
  anio: number;
  color?: string;
  cliente: Cliente;
}

export interface OrdenTrabajo {
  id: number;
  vehiculo: Vehiculo;
  mecanico: Mecanico;
  descripcionProblema: string;
  diagnostico?: string;
  estado: 'PENDIENTE' | 'EN_PROCESO' | 'DIAGNOSTICADO' | 'REPARADO' | 'LISTO' | 'ENTREGADO';
  precioEstimado?: number;
  precioFinal?: number;
  fechaEntrada: string;
  fechaSalidaEstimada?: string;
}

export interface Ausencia {
  id: number;
  mecanico: Mecanico;
  tipo: 'VACACIONES' | 'DIA_LIBRE' | 'BAJA' | 'OTRO';
  fechaInicio: string;
  fechaFin: string;
  motivo?: string;
  aprobada?: boolean;
}

export interface AuthResponse {
  token: string;
  id: number;
  nombre: string;
  apellidos: string;
  email: string;
  rol: string;
  mecanicoId?: number;
  clienteId?: number;
}

export interface LoginRequest {
  email: string;
  password: string;
}