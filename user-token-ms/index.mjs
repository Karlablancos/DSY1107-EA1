const SCOPES_POR_GRUPO = {
  solicitante: ['gastos/read', 'gastos/write'],
  aprobador:   ['gastos/read', 'gastos/write'],
};

export const handler = async (event) => {
  const grupos = event.request.groupConfiguration?.groupsToOverride ?? [];

  const scopes = [...new Set(grupos.flatMap((grupo) => SCOPES_POR_GRUPO[grupo] ?? []))];

  console.log(
    JSON.stringify({
      usuario: event.request.userAttributes?.email ?? event.userName,
      grupos,
      scopes,
    }),
  );

  event.response = {
    claimsAndScopeOverrideDetails: {
      accessTokenGeneration: {
        scopesToAdd: scopes,
      },
    },
  };

  return event;
};