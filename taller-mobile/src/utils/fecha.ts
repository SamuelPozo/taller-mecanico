export const formatFecha = (fecha: string | undefined | null): string => {
  if (!fecha) return '—';
  const parte = fecha.substring(0, 10);
  const [year, month, day] = parte.split('-');
  return `${day}/${month}/${year}`;
};

export const formatFechaHora = (fecha: string | undefined | null): string => {
  if (!fecha) return '—';
  const parte = fecha.substring(0, 16);
  const [date, time] = parte.split('T');
  const [year, month, day] = date.split('-');
  return `${day}/${month}/${year} ${time}`;
};