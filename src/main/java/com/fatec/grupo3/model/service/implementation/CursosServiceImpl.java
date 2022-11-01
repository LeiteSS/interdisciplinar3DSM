package com.fatec.grupo3.model.service.implementation;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.transaction.Transactional;

import com.fatec.grupo3.exception.AreaProibidaException;
import com.fatec.grupo3.model.dto.CursoDTO;
import com.fatec.grupo3.model.dto.UsuarioDTO;
import com.fatec.grupo3.model.entities.Aula;
import com.fatec.grupo3.model.entities.Exercicio;
import com.fatec.grupo3.model.entities.Usuario;
import com.fatec.grupo3.model.mapper.AulasMapper;
import com.fatec.grupo3.model.mapper.CursosMapper;
import com.fatec.grupo3.model.mapper.ExerciciosMapper;
import com.fatec.grupo3.model.mapper.UsuariosMapper;
import com.fatec.grupo3.model.repositories.AulasRepository;
import com.fatec.grupo3.model.repositories.ExercicioRepository;
import com.fatec.grupo3.model.repositories.UsuariosRepository;
import com.fatec.grupo3.security.TokenService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;


import com.fatec.grupo3.model.entities.Curso;
import com.fatec.grupo3.model.repositories.CursosRepositories;
import com.fatec.grupo3.model.service.CursosService;

@Service
public class CursosServiceImpl implements CursosService {

    Logger logger = LogManager.getLogger(this.getClass());

    @Autowired
    private CursosRepositories repository;

    @Autowired
    private AulasRepository aulasRepository;

    @Autowired
    private ExercicioRepository exercicioRepository;

    @Autowired
    private UsuariosRepository usuariosRepository;

    @Autowired
    private TokenService tokenService;

    @Autowired
    private CursosMapper mapper = CursosMapper.INSTANCE;

    @Autowired
    private UsuariosMapper usuariosMapper = UsuariosMapper.INSTANCE;

    @Autowired
    private AulasMapper aulasMapper = AulasMapper.INSTANCE;

    @Autowired
    private ExerciciosMapper exerciciosMapper = ExerciciosMapper.INSTANCE;

    @Override
    public List<CursoDTO> consultaTodos() {
        logger.info(">>>>>> servico consultaTodos chamado");
        return repository.findAll()
                .stream()
                .map(mapper::toDTO)
                .collect(Collectors.toList());
    }

    @Override
    public Optional<CursoDTO> consultaPorTitulo(String titulo) {
        logger.info(">>>>>> servico consultaPorTitulo chamado");
        Optional<Curso> curso = repository.findCursoByTitulo(titulo);
        return Optional.of(mapper.toDTO(curso.get()));
    }
    
    @Transactional
    @Override
    public Optional<CursoDTO> save(CursoDTO cursoDTO, String token) {
        Long userId = tokenService.getUserId(token);

        Usuario usuario = usuariosRepository.getReferenceById(userId);

        Curso curso = mapper.toModel(cursoDTO);


        if (curso.getAulas() != null) {
            for(Aula aula : curso.getAulas()) {
                aulasRepository.save(aula);
            }
        }

        if (curso.getExercicios() != null) {
            for(Exercicio exercicio : curso.getExercicios()) {
                exercicioRepository.save(exercicio);
            }
        }
        curso.setUsuario(usuario);
        Curso cursoSalvo = repository.save(curso);

        //UsuarioDTO usuarioLogado = usuariosMapper.toDTO(usuario);

        logger.info(">>>>>> servico save chamado ");

        return Optional.ofNullable(mapper.toDTO(cursoSalvo));
    }

    @Override
    @Transactional
    public void delete(Long id, String token) {
        Long userId = tokenService.getUserId(token);
        Usuario usuario = usuariosRepository.getReferenceById(userId);

        repository.deleteByUsuarioAndCursoId(usuario, id);
    }

    @Override
    @Transactional
    public Optional<CursoDTO> atualiza(Long id, CursoDTO cursoDTO, String token) {
        Long userId = tokenService.getUserId(token);

        Usuario usuario = usuariosRepository.getReferenceById(userId);
        
        Curso curso = mapper.toModel(cursoDTO);

        Curso cursoFounded = repository.getReferenceById(id);

        if (cursoFounded.getAulas() != null) {

            for(Aula aula : cursoFounded.getAulas()) {

                aulasRepository.save(aula);
            }
        }

        if (cursoFounded.getExercicios() != null) {

            for(Exercicio exercicio : cursoFounded.getExercicios()) {

                exercicioRepository.save(exercicio);
            }
        }

        curso.setCursoId(id);
        curso.setUsuario(usuario);
        Curso cursoSalvo = repository.save(curso);

        //UsuarioDTO usuarioLogado = usuariosMapper.toDTO(usuario);

        logger.info(">>>>>> servico atualiza chamado ");

        return Optional.ofNullable(mapper.toDTO(cursoSalvo));
    }

    @Override
    public Optional<CursoDTO> consultarPorId(Long id, String token) throws AreaProibidaException {
        Long userId = tokenService.getUserId(token);

        Usuario usuario = usuariosRepository.getReferenceById(userId);

        if (usuario.getRoles().contains("INSTRUTOR")) {
            Optional<Curso> curso = repository.findById(id);
            return Optional.of(mapper.toDTO(curso.get()));
        }

        throw new AreaProibidaException(usuario.getCpf());
    }

}
