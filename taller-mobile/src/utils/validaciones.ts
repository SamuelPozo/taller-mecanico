export const validarEmail = (email: string): boolean => {
  const regex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
  return regex.test(email.trim());
};

export const validarNIF = (nif: string): boolean => {
  const regex = /^[0-9]{8}[A-Za-z]$/;
  if (!regex.test(nif.trim())) return false;
  const letras = 'TRWAGMYFPDXBNJZSQVHLCKE';
  const numero = parseInt(nif.substring(0, 8), 10);
  const letraCorrecta = letras[numero % 23];
  return nif.toUpperCase().charAt(8) === letraCorrecta;
};

export const validarMatricula = (matricula: string): boolean => {
  // Formato español actual: 4 números + 3 letras (ej: 1234ABC)
  const regex = /^[0-9]{4}[A-Za-z]{3}$/;
  return regex.test(matricula.trim());
};

export const validarTelefono = (telefono: string): boolean => {
  const regex = /^[6-9][0-9]{8}$/;
  return regex.test(telefono.trim());
};