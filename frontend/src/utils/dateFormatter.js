export const formatExpirationDate = (isoString) => {
  if (!isoString) return 'Never';
  return new Date(isoString).toLocaleDateString(undefined, {
    month: 'short',
    day: 'numeric',
    year: 'numeric'
  });
};

export const formatAnalyticsDate = (isoString) => {
  if (!isoString) return 'No clicks yet';
  return new Date(isoString).toLocaleString(undefined, {
    dateStyle: 'medium',
    timeStyle: 'short'
  });
};
